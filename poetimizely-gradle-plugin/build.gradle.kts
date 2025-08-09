group = "io.github.patxibocos"
version = libs.versions.poetimizely.get()

plugins {
    jacoco
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(libs.poetimizely.core)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core.jvm)
    testImplementation(gradleTestKit())
}

kotlin {
    jvmToolchain(17)
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("${layout.buildDirectory}/**/*.kt", "bin/**/*.kt")
        ktlint(libs.versions.ktlint.get())
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
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
        xml.outputLocation.set(file("${layout.buildDirectory}/reports/jacoco/test/jacoco.xml"))
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
