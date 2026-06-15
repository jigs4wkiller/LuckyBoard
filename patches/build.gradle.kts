group = "dev.lucky.gboardpatches"

val generatedPatchInfoDir = layout.buildDirectory.dir("generated/sources/patchBuildInfo/kotlin/main")
val generatedPreviewAssetsResourcesDir = layout.buildDirectory.dir("generated/resources/previewAssets/main")
val patchMetadataSourceSet = sourceSets.create("patchMetadata") {
    java.srcDir("src/patchMetadata/kotlin")
}
val utf8Bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
val previewAssetsSourceDir = layout.projectDirectory.dir("src/main/resources/settings-previews")

configurations.named(patchMetadataSourceSet.implementationConfigurationName) {
    extendsFrom(configurations["implementation"])
}

// Configuration to resolve pngtastic for stripping (we exclude the ant subpackage
// that causes NoClassDefFound for org.apache.tools.ant.Task in the patcher runtime
// used by both CLI and Morphe app).
val pngtasticConfig = configurations.create("pngtasticForStrip") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = true
}

dependencies {
    pngtasticConfig("com.github.depsypher:pngtastic:1.8")
}

// Task that produces a pngtastic jar with the problematic ant/ package removed.
// This stripped jar is then used as implementation so the final .mpp never contains
// the ant classes that break patch loading in Morphe/CLI.
val pngtasticStrippedJar by tasks.registering(Jar::class) {
    group = "build"
    description = "Produce pngtastic jar without ant/ subpackage (for Morphe compatibility)"

    val pngtasticArtifact = pngtasticConfig.singleFile
    inputs.file(pngtasticArtifact)

    archiveBaseName.set("pngtastic-stripped")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))

    from(zipTree(pngtasticArtifact)) {
        exclude("com/googlecode/pngtastic/ant/**")
    }

    // Also exclude any META-INF/maven for the ant part if present, but the exclude above is sufficient.
}

dependencies {
    // Use the stripped pngtastic for both compilation of our patches and inclusion in the .mpp.
    // This avoids shipping the ant/ classes that the patcher classloaders (in CLI and app) cannot resolve.
    implementation(files(pngtasticStrippedJar))
}

val generatePatchBuildInfo by tasks.registering {
    val outputDir = generatedPatchInfoDir
    val patchVersion = project.version.toString()

    inputs.property("patchVersion", patchVersion)
    outputs.dir(outputDir)

    doLast {
        val packageDir = outputDir.get().file("dev/jason/gboardpatches/patches/shared").asFile
        packageDir.mkdirs()

        packageDir.resolve("PatchBuildInfo.kt").writeText(
            """
            package dev.lucky.gboardpatches.patches.shared

            internal object PatchBuildInfo {
                const val VERSION = "$patchVersion"
            }
            """.trimIndent()
        )
    }
}

val generatePreviewAssetsIndex by tasks.registering {
    val sourceDir = previewAssetsSourceDir
    val outputFile = generatedPreviewAssetsResourcesDir.map { directory ->
        directory.file("settings-previews/index.txt")
    }

    inputs.dir(sourceDir)
    outputs.file(outputFile)

    doLast {
        val sourceRoot = sourceDir.asFile
        if (!sourceRoot.exists()) {
            throw GradleException("Preview assets directory not found: $sourceRoot")
        }

        val indexedAssets = sourceRoot.walkTopDown()
            .filter { file -> file.isFile && file.name != "index.txt" }
            .map { file -> file.relativeTo(sourceRoot).invariantSeparatorsPath }
            .sorted()
            .toList()

        if (indexedAssets.isEmpty()) {
            throw GradleException("No preview assets found under $sourceRoot")
        }

        val output = outputFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(
            indexedAssets.joinToString(
                separator = System.lineSeparator(),
                postfix = System.lineSeparator()
            ),
            Charsets.UTF_8
        )
    }
}

patches {
    about {
        name = "LuckyBoard"
        description = "Morphe patches for Gboard, rebranded as LuckyBoard with experimental features and incognito enhancements."
        source = "https://github.com/jigs4wkiller/LuckyBoard"
        author = "jigs4wkiller"
        contact = "https://github.com/jigs4wkiller/LuckyBoard/issues"
        website = "https://github.com/jigs4wkiller/LuckyBoard"
        license = "GPLv3"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

sourceSets.named("main") {
    java.srcDir(generatedPatchInfoDir)
    resources.srcDir(generatedPreviewAssetsResourcesDir)
}

dependencies {
    add(patchMetadataSourceSet.implementationConfigurationName, libs.gson)
    // pngtastic for the Universal PNG Optimizer patch (real optimization in Morphe runtime).
    // Stripped at dep time (see top of file) + post-build safety strip to remove ant/ classes.
    implementation("com.github.depsypher:pngtastic:1.8")
    testImplementation("junit:junit:4.13.2")
}

tasks {
    named("compileKotlin") {
        dependsOn(generatePatchBuildInfo)
    }

    named("processResources") {
        dependsOn(generatePreviewAssetsIndex)
    }

    named("sourcesJar") {
        dependsOn(
            generatePatchBuildInfo,
            generatePreviewAssetsIndex
        )
    }

    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build, patchMetadataSourceSet.classesTaskName)

        // Include ant explicitly because loadPatchesFromJar / PatchLoader (from morphe) requires
        // org.apache.tools.ant.Task at runtime for the generator (otherwise NoClassDefFoundError).
        val antConfiguration = project.configurations.detachedConfiguration(
            project.dependencies.create("org.apache.ant:ant:1.10.13")
        )
        classpath = patchMetadataSourceSet.runtimeClasspath + project.files(antConfiguration.resolve())
        mainClass.set("dev.lucky.gboardpatches.util.PatchListGeneratorKt")
    }

    register("normalizePatchMetadataEncoding") {
        description = "Ensures generated patch metadata JSON files are encoded as UTF-8 without BOM."

        doLast {
            listOf(
                rootProject.file("patches-bundle.json"),
                rootProject.file("patches-list.json"),
            ).forEach { jsonFile ->
                if (!jsonFile.exists()) {
                    return@forEach
                }

                val bytes = jsonFile.readBytes()
                val hasUtf8Bom =
                    bytes.size >= utf8Bom.size &&
                        utf8Bom.indices.all { index -> bytes[index] == utf8Bom[index] }

                if (hasUtf8Bom) {
                    jsonFile.writeBytes(bytes.copyOfRange(utf8Bom.size, bytes.size))
                }
            }
        }
    }

    named("generatePatchesList") {
        finalizedBy("normalizePatchMetadataEncoding")
    }
    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}

// Post-build strip of pngtastic ant/ classes from the final .mpp.
// This removes the classes that reference missing org.apache.tools.ant.Task (causing the app/CLI
// NoClassDefFoundError on load), while keeping the core PngOptimizer etc. that our patch uses.
// We re-zip the mpp (python) after removing only the bad .class entries. The rve and other
// resources stay intact.
tasks.named("buildAndroid").configure {
    doLast {
        val mpp = layout.buildDirectory.file("libs/patches-${version}.mpp").get().asFile
        if (mpp.exists()) {
            println("Stripping pngtastic/ant/ classes from $mpp (for Morphe app/CLI compatibility)...")
            val code = """
import zipfile, os, shutil
src = '${mpp.absolutePath}'
tmp = src + '.strip'
with zipfile.ZipFile(src, 'r') as zin:
    with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            if not item.filename.startswith('com/googlecode/pngtastic/ant/'):
                zout.writestr(item, zin.read(item.filename))
os.replace(tmp, src)
print('strip complete, size:', os.path.getsize(src))
"""
            val p = Runtime.getRuntime().exec(arrayOf("python3", "-c", code))
            val exit = p.waitFor()
            if (exit != 0) {
                println(p.errorStream.bufferedReader().readText())
            }
        }
    }
}
