group = "io.github.patxibocos"
version = "1.0.5"

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.1.0"
    jacoco
    id("com.diffplug.spotless") version "6.18.0"
    kotlin("jvm") version "1.8.20"
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("io.github.patxibocos:poetimizely-core:$version")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.5")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.5")
    testImplementation(gradleTestKit())
}

kotlin {
    jvmToolchain(11)
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("$buildDir/**/*.kt", "bin/**/*.kt")
        ktlint("0.48.2")
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint("0.48.2")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy("jacocoTestReport")
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        csv.required.set(false)
        xml.outputLocation.set(file("$buildDir/reports/jacoco/test/jacoco.xml"))
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

gradlePlugin {
    website.set("https://github.com/patxibocos/poetimizely")
    vcsUrl.set("https://github.com/patxibocos/poetimizely")
    plugins {
        create("poetimizely") {
            id = "io.github.patxibocos.poetimizely"
            displayName = "poetimizely"
            description = "Generate Kotlin type safe accessors for Optimizely experiments and features"
            implementationClass = "io.github.patxibocos.poetimizely.gradle.plugin.PoetimizelyPlugin"
            tags.set(listOf("optimizely", "kotlin", "typesafe", "kotlinpoet"))
        }
    }
}
