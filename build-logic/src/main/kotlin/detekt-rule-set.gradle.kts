import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("code-coverage")
    id("com.gradleup.shadow")
    id("jvm-lib")
    id("static-analysis")
}

val libs = the<LibrariesForLibs>()

dependencies {
    compileOnly(libs.detekt.api)

    testImplementation(libs.detekt.api)
    testImplementation(libs.detekt.test)
    testImplementation(libs.kotlin.test)
}

tasks.shadowJar {
    archiveBaseName = "intellij-inspections-${project.path.removePrefix(":rules:").replace(':', '-')}"
    archiveClassifier = ""
    archiveVersion = project.version.toString()
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    manifest {
        attributes(
            "Implementation-Title" to archiveBaseName.get(),
            "Implementation-Version" to archiveVersion.get(),
        )
    }
}
