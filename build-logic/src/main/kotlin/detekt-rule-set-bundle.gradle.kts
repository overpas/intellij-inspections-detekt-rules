plugins {
    java
    id("com.gradleup.shadow")
}

val ruleSetGroup = project.name
val ruleSetGroupPath = ":rules:$ruleSetGroup"

dependencies {
    rootProject.subprojects
        .filter { it.parent?.path == ruleSetGroupPath }
        .sortedBy { it.path }
        .forEach { implementation(project(it.path)) }
}

tasks.shadowJar {
    archiveBaseName = "intellij-inspections-$ruleSetGroup-all"
    archiveClassifier = ""
    archiveVersion = project.version.toString()
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    mergeServiceFiles()
    manifest {
        attributes(
            "Implementation-Title" to archiveBaseName.get(),
            "Implementation-Version" to archiveVersion.get(),
        )
    }
}
