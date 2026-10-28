import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    id("code-coverage")
    id("maven-central")
}

val ruleSets = listOf(
    "android:compose-preview",
    "android:glance-preview",
    "android:kotlin",
    "android:wear-tile-preview",
    "compose:multiplatform-preview",
    "gradle:best-practices",
    "jvm:code-maturity",
    "jvm:declaration-redundancy",
    "jvm:general",
    "jvm:junit",
    "jvm:logging",
    "jvm:test-frameworks",
    "kotlin:code-migration",
    "kotlin:coroutines",
    "kotlin:java-interop",
    "kotlin:logging",
    "kotlin:naming-conventions",
    "kotlin:numeric-issues",
    "kotlin:other-problems",
    "kotlin:probable-bugs",
    "kotlin:redundant-constructs",
    "kotlin:style-issues",
)

val bundles = listOf(
    "android",
    "jvm",
    "kotlin",
)

val ruleSetJars by configurations.creating {
    isCanBeConsumed = false
    isTransitive = false
}

dependencies {
    ruleSets.forEach { kover(project(":rules:$it")) }

    ruleSets.forEach { ruleSetJars(project(":rules:$it", "shadowRuntimeElements")) }

    bundles.forEach { ruleSetJars(project(":bundles:$it", "shadowRuntimeElements")) }
}

kover {
    reports {
        total {
            verify {
                rule {
                    minBound(80, CoverageUnit.LINE)
                }
            }
        }
    }
}

tasks.register<Sync>("ruleJars") {
    group = "build"
    description = "Collects the versioned rule set and bundle jars into build/releases/<version>."
    from(ruleSetJars)
    into(layout.buildDirectory.dir("releases/$version"))
}
