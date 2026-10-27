plugins {
    java
    `maven-publish`
    signing
    id("com.gradleup.shadow")
}

java {
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = MavenCentral.STAGING_REPOSITORY_NAME
            url = uri(rootProject.layout.buildDirectory.dir(MavenCentral.STAGING_DIRECTORY))
        }
    }

    publications.create<MavenPublication>("maven") {
        from(components["shadow"])
        artifact(tasks.named("sourcesJar"))
        groupId = providers.gradleProperty("lib.group").get()
    }

    publications.withType<MavenPublication>().configureEach {
        val publicationName = name
        val javadocJar = tasks.register<Jar>("${publicationName}JavadocJar") {
            archiveClassifier.set("javadoc")
            destinationDirectory.set(layout.buildDirectory.dir("javadoc/$publicationName"))
        }
        artifact(javadocJar)

        pom {
            name.set(provider { artifactId })
            description.set(provider { project.description })
            url.set("https://github.com/overpas/intellij-inspections-detekt-rules")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("overpas")
                    name.set("Pavel Shurmilov")
                    email.set("pckeycalculator@gmail.com")
                }
            }

            scm {
                connection.set("scm:git:https://github.com/overpas/intellij-inspections-detekt-rules.git")
                developerConnection.set("scm:git:ssh://git@github.com/overpas/intellij-inspections-detekt-rules.git")
                url.set("https://github.com/overpas/intellij-inspections-detekt-rules/tree/main")
            }
        }
    }
}

val signingKey = providers.gradleProperty("signingInMemoryKey")

signing {
    if (signingKey.isPresent) {
        useInMemoryPgpKeys(
            providers.gradleProperty("signingInMemoryKeyId").orNull,
            signingKey.get(),
            providers.gradleProperty("signingInMemoryKeyPassword").orNull,
        )
    }
    isRequired = signingKey.isPresent || providers.gradleProperty("signing.keyId").isPresent
    sign(publishing.publications)
}

tasks.withType<PublishToMavenRepository>()
    .matching { it.name.endsWith(MavenCentral.STAGING_PUBLISH_TASK_SUFFIX) }
    .configureEach {
        dependsOn(":${MavenCentral.CLEAN_STAGING_TASK}")
    }
