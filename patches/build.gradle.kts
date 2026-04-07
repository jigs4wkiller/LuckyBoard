group = "dev.jason.gboardpatches"

val generatedPatchInfoDir = layout.buildDirectory.dir("generated/sources/patchBuildInfo/kotlin/main")

val generatePatchBuildInfo by tasks.registering {
    val outputDir = generatedPatchInfoDir

    outputs.dir(outputDir)

    doLast {
        val packageDir = outputDir.get().file("dev/jason/gboardpatches/patches/shared").asFile
        packageDir.mkdirs()

        packageDir.resolve("PatchBuildInfo.kt").writeText(
            """
            package dev.jason.gboardpatches.patches.shared

            internal object PatchBuildInfo {
                const val VERSION = "${project.version}"
            }
            """.trimIndent()
        )
    }
}

patches {
    about {
        name = "Gboard Patches"
        description = "Morphe patches for Gboard."
        source = "https://github.com/jasonwu1994/gboard-patches"
        author = "Jason Wu"
        contact = "https://github.com/jasonwu1994/gboard-patches/issues"
        website = "https://github.com/jasonwu1994/gboard-patches"
        license = "GPLv3"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

sourceSets.named("main") {
    java.srcDir(generatedPatchInfoDir)
}

dependencies {
    // Used by JsonGenerator.
    implementation(libs.gson)
}

tasks {
    named("compileKotlin") {
        dependsOn(generatePatchBuildInfo)
    }

    named("sourcesJar") {
        dependsOn(generatePatchBuildInfo)
    }

    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("dev.jason.gboardpatches.util.PatchListGeneratorKt")
    }
    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
