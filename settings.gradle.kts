rootProject.name = "intellij-inspections-detekt-rules"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":annotations")
include(":bundles:kotlin")
include(":rules:kotlin:code-migration")
include(":rules:kotlin:coroutines")
include(":rules:kotlin:java-interop")
include(":rules:kotlin:logging")
include(":rules:kotlin:migration")
include(":rules:kotlin:naming-conventions")
include(":rules:kotlin:numeric-issues")
include(":rules:kotlin:other-problems")
include(":rules:kotlin:probable-bugs")
include(":rules:kotlin:redundant-constructs")
include(":rules:kotlin:style-issues")
