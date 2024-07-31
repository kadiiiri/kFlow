import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
    application
}


group = "com.github"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val kubernetes = "21.0.0"
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

application {
    mainClass.set("MainKt")
}