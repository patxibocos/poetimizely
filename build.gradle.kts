import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

plugins {
    id("application")
    kotlin("jvm") version "1.3.70"
    kotlin("plugin.serialization") version "1.3.70"
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.0.0-BETA3")
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
    }
    apply(file("gradle/ktlint.gradle.kts"))
}

val experimentGeneratedCodeData = mapOf(
    "path" to "src/test/kotlin/Experiment.kt",
    "experimentKey" to "TEST_EXPERIMENT",
    "variationKey" to "TEST_VARIATION"
)
val serializedData: String = run {
    val json = Json(kotlinx.serialization.json.JsonConfiguration.Stable)
    val mapSerializer = MapSerializer(String.serializer(), String.serializer())
    json.stringify(mapSerializer, experimentGeneratedCodeData)
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("experimentGeneratedCodeData", serializedData)
}

tasks.named<Test>("test") {
    exclude("**/GeneratorTestSetup.class")
    doLast {
        delete(experimentGeneratedCodeData["path"])
    }
}

tasks.register<Test>("generatorSetup") {
    include("**/GeneratorTestSetup.class")
}
