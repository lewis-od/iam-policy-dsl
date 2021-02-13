plugins {
    base
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("org.jetbrains.dokka") version "1.4.20"
    `java-library`
    `maven-publish`
    signing
}

object ProjectInfo {
    val version = "1.0"
    val artifactId = "iam-policy-dsl"
    val description = "A Kotlin DSL for declaring AWS IAM policy documents"

    val url = "https://github.com/lewis-od/iam-policy-dsl"
    val scm = "scm:git:$url"
}

group = "com.github.lewisod"
version = ProjectInfo.version

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    testImplementation("org.assertj:assertj-core:3.9.1")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.lewis-od"
            version = ProjectInfo.version
            artifactId = ProjectInfo.artifactId
            pom {
                name.set(ProjectInfo.artifactId)
                description.set(ProjectInfo.description)
                url.set(ProjectInfo.url)
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Lewis O'Driscoll")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("${ProjectInfo.url}/issues")
                }
                scm {
                    connection.set(ProjectInfo.scm)
                    url.set(ProjectInfo.url)
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            url = uri(
                if (ProjectInfo.version.contains("SNAPSHOT"))
                    "https://oss.sonatype.org/content/repositories/snapshots/"
                else
                    "https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("SONATYPE_USER")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

with(signing) {
    val signingKey = System.getenv("SIGNING_KEY")
    val signingKeyPassword = System.getenv("SIGNING_KEY_PASSWORD")
    useInMemoryPgpKeys(signingKey, signingKeyPassword)
    sign(publishing.publications.getByName("maven"))
}
