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

include(":rules:code-migration")
include(":rules:coroutines")
include(":rules:java-interop")
include(":rules:logging")
include(":rules:migration")
include(":rules:naming-conventions")
include(":rules:numeric-issues")
include(":rules:other-problems")
include(":rules:probable-bugs")
include(":rules:redundant-constructs")
include(":rules:style-issues")
