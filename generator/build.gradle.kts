import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.patxi"
version = "1.0.0"

plugins {
    jacoco
    `maven-publish`
    kotlin("jvm") version "1.3.72"
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

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.2.7")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.0.5")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.0.5")
    testImplementation("io.mockk:mockk:1.9.3")
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

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        xml.destination = file("${buildDir}/reports/jacoco/test/jacocoTestReport.xml")
    }
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}