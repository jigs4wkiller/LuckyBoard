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

dependencies {
    add(patchMetadataSourceSet.implementationConfigurationName, libs.gson)
    // No external libs for PNG optimizer on this branch (pure JDK impl)
    testImplementation("junit:junit:4.13.2")
}

val generatePatchBuildInfo by tasks.registering {
    val outputDir = generatedPatchInfoDir
    val patchVersion = project.version.toString()
    inputs.property("patchVersion", patchVersion)
    outputs.dir(outputDir)
    doLast {
        val packageDir = outputDir.get().file("dev/lucky/gboardpatches/patches/shared").asFile
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
        description = "Morphe patches for Gboard (beta branch with PNG optimizer for new impl development)."
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
        classpath = patchMetadataSourceSet.runtimeClasspath
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
    publish {
        dependsOn("generatePatchesList")
    }
}

// No post-build pngtastic strip needed on this branch (pure JDK PNG impl, no ext lib).
