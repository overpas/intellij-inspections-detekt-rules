import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    id("code-coverage")
}

val ruleSets = listOf(
    "kotlin:code-migration",
    "kotlin:coroutines",
    "kotlin:java-interop",
    "kotlin:logging",
    "kotlin:migration",
    "kotlin:naming-conventions",
    "kotlin:numeric-issues",
    "kotlin:other-problems",
    "kotlin:probable-bugs",
    "kotlin:redundant-constructs",
    "kotlin:style-issues",
)

val ruleSetJars by configurations.creating {
    isCanBeConsumed = false
    isTransitive = false
}

dependencies {
    ruleSets.forEach { kover(project(":rules:$it")) }

    ruleSets.forEach { ruleSetJars(project(":rules:$it", "shadowRuntimeElements")) }
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
    description = "Collects the versioned rule set jars into build/releases/<version>."
    from(ruleSetJars)
    into(layout.buildDirectory.dir("releases/$version"))
}
