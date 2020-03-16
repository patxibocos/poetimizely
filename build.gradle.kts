plugins {
    id("application")
    kotlin("jvm") version "1.3.70"
}

application {
    mainClassName = "com.patxi.poetimizely.Main"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.optimizely.ab:core-api:3.4.1")
    implementation("com.squareup:kotlinpoet:1.5.0")
    implementation("com.squareup.retrofit2:retrofit:2.7.2")
    implementation("com.squareup.retrofit2:converter-gson:2.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.0.0-BETA2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}