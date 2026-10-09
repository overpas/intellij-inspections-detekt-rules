import dev.detekt.gradle.Detekt
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("dev.detekt")
}

val libs = the<LibrariesForLibs>()

detekt {
    parallel = true
    buildUponDefaultConfig = true
    basePath = rootProject.layout.projectDirectory
    config.setFrom(rootProject.layout.projectDirectory.file("config/detekt/detekt.yml"))
    source.setFrom(layout.projectDirectory.dir("src"), layout.projectDirectory.file("build.gradle.kts"))
}

dependencies {
    detektPlugins(project(":rules:kotlin:code-migration"))
    detektPlugins(project(":rules:kotlin:coroutines"))
    detektPlugins(project(":rules:kotlin:java-interop"))
    detektPlugins(project(":rules:kotlin:logging"))
    detektPlugins(project(":rules:kotlin:migration"))
    detektPlugins(project(":rules:kotlin:naming-conventions"))
    detektPlugins(project(":rules:kotlin:numeric-issues"))
    detektPlugins(project(":rules:kotlin:other-problems"))
    detektPlugins(project(":rules:kotlin:probable-bugs"))
    detektPlugins(project(":rules:kotlin:redundant-constructs"))
    detektPlugins(project(":rules:kotlin:style-issues"))

    detektPlugins(
        fileTree(rootProject.layout.projectDirectory.dir("config/detekt/plugins")) { include("*.jar") },
    )

    detektPlugins(libs.detekt.ktlint.wrapper)
}

tasks.withType<Detekt>().configureEach {
    autoCorrect = true
    reports {
        html.required = true
        markdown.required = true
        sarif.required = false
        checkstyle.required = false
    }
}

val typeResolutionTasks = tasks.withType<Detekt>().matching {
    it.name != "detekt" && !it.name.endsWith("SourceSet")
}

typeResolutionTasks.configureEach {
    val buildDir = layout.buildDirectory.get().asFile
    autoCorrect = false
    buildUponDefaultConfig = false
    parallel = false
    config.setFrom(rootProject.layout.projectDirectory.file("config/detekt/detekt-type-resolution.yml"))
    exclude { it.file.startsWith(buildDir) }
}

tasks.named("detekt") {
    dependsOn(typeResolutionTasks)
}
