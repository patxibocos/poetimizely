plugins {
    id("application")
    kotlin("jvm") version "1.3.70"
}

application {
    mainClassName = "com.patxi.poetimizely.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup:kotlinpoet:1.5.0")
}