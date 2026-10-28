import org.gradle.accessors.dm.LibrariesForLibs
import java.util.zip.ZipFile

plugins {
    id("code-coverage")
    id("com.gradleup.shadow")
    id("jvm-lib")
    id("publish")
    id("static-analysis")
}

val libs = the<LibrariesForLibs>()
val ruleSetName = project.path.removePrefix(":rules:").replace(':', '-')
val artifactName = "intellij-inspections-$ruleSetName"

description = "The intellij-$ruleSetName detekt rule set, ported from the IntelliJ IDEA inspections"

publishing {
    publications.named<MavenPublication>("maven") {
        artifactId = artifactName
    }
}

dependencies {
    compileOnly(project(":annotations"))
    compileOnly(libs.detekt.api)

    testImplementation(libs.detekt.api)
    testImplementation(libs.detekt.test)
    testImplementation(libs.kotlin.test)
}

tasks.shadowJar {
    archiveBaseName = artifactName
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

val verifyDefaultConfig by tasks.registering {
    group = "verification"
    description = "Checks that the rule set jar contains the default config."
    val jar = tasks.shadowJar.flatMap { it.archiveFile }
    inputs.file(jar)
    doLast {
        ZipFile(jar.get().asFile).use { zip ->
            check(zip.getEntry("config/config.yml") != null) {
                "${jar.get().asFile.name} does not contain config/config.yml"
            }
        }
    }
}

val verifyUpstreamDefaults by tasks.registering {
    group = "verification"
    description = "Checks that the default config activates the rules as IntelliJ IDEA enables them by default."
    val modulePath = project.path
    val catalog = rootProject.layout.projectDirectory.file("docs/inspections.csv")
    val overrides = rootProject.layout.projectDirectory.file("config/upstream-default-overrides.csv")
    val config = layout.projectDirectory.file("src/main/resources/config/config.yml")
    inputs.file(catalog)
    inputs.file(overrides)
    inputs.file(config)
    doLast {
        val column = Regex(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")
        val rows = catalog.asFile.readLines().map { it.split(column) }
        val header = rows.first()
        val upstreamEnabled = rows.drop(1)
            .map { row -> header.zip(row).toMap() }
            .filter { it["module"] == modulePath && it["status"].orEmpty().startsWith("ported:") }
            .flatMap { row ->
                Regex("`(\\w+)`").findAll(row["status"].orEmpty())
                    .map { it.groupValues[1] to (row["enabledByDefault"] == "true") }
            }
            .toMap()
        val ruleEntry = Regex("^ {2}(\\w+):$")
        val activeEntry = Regex("^ {4}active: (\\w+)$")
        val active = buildMap {
            var rule: String? = null
            config.asFile.readLines().forEach { line ->
                ruleEntry.find(line)?.let { rule = it.groupValues[1] }
                activeEntry.find(line)?.let { entry -> rule?.let { put(it, entry.groupValues[1] == "true") } }
            }
        }
        val overridden = overrides.asFile.readLines().drop(1).map { it.substringBefore(',') }.toSet()
        val violations = upstreamEnabled.filter { (rule, enabled) -> active[rule] != enabled && rule !in overridden }
        check(violations.isEmpty()) {
            "$modulePath sets a default state that differs from IntelliJ IDEA: " +
                violations.entries.joinToString { (rule, enabled) -> "$rule (upstream active: $enabled)" } + ". " +
                "Match the upstream state or list the rules in config/upstream-default-overrides.csv."
        }
    }
}

tasks.check {
    dependsOn(verifyDefaultConfig, verifyUpstreamDefaults)
}
