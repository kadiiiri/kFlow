import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "com.github"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val kubernetes = "15.0.1"
    val kotlinLogging = "7.0.0"
    val slf4jSimple = "2.0.13"

    implementation("io.kubernetes:client-java:${kubernetes}")
    implementation("io.github.oshai:kotlin-logging:${kotlinLogging}")
    implementation("org.slf4j:slf4j-simple:${slf4jSimple}")


    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}