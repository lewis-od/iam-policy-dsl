plugins {
    base
    kotlin("jvm") version "1.4.10"
}

group = "uk.co.lewisod"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    testImplementation("org.assertj:assertj-core:3.9.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
