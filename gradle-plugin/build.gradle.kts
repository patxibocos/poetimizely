import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.patxi"
version = "1.0.0"

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.12.0"
    jacoco
    kotlin("jvm") version "1.4.20"
}

repositories {
    mavenCentral()
    jcenter()
    mavenLocal()
}

dependencies {
    implementation("com.patxi:poetimizely-generator:$version")
    implementation("com.optimizely.ab:core-api:3.7.0")
    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.2.9")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.3.1")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.3.1")
    testImplementation("io.mockk:mockk:1.10.2")
    testImplementation(gradleTestKit())
}

buildscript {
    apply(file("gradle/ktlint.gradle.kts"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
    finalizedBy("jacocoTestReport")
}

gradlePlugin {
    plugins {
        create("poetimizely") {
            id = "com.patxi.poetimizely"
            displayName = "poetimizely"
            description = "Generate Kotlin type safe accessors for Optimizely experiments and features"
            implementationClass = "com.patxi.poetimizely.gradle.plugin.PoetimizelyPlugin"
        }
    }
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        xml.destination = file("${buildDir}/reports/jacoco/test/jacocoTestReport.xml")
    }
}

pluginBundle {
    website = "https://github.com/patxibocos/poetimizely"
    vcsUrl = "https://github.com/patxibocos/poetimizely"
    tags = listOf("optimizely", "kotlin", "typesafe", "kotlinpoet")
}