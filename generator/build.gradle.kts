import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "io.github.patxibocos"
version = "1.0.1"

plugins {
    jacoco
    `maven-publish`
    signing
    id("com.diffplug.spotless") version ("5.12.5")
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.optimizely.ab:core-api:3.8.2")
    implementation("com.squareup:kotlinpoet:1.8.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.1")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.6.0")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.6.0")
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation(gradleTestKit())
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    kotlin {
        target("**/*.kt")
        ktlint("0.41.0")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

jacoco {
    toolVersion = "0.8.7"
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

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("poetimizelyGenerator") {
            from(components["java"])
            pom.withXml {
                val root = asNode()
                root.appendNode("name", project.name)
                root.appendNode(
                    "description",
                    "poetimizely generator module that both Gradle and Maven plugins depend on"
                )
                root.appendNode("url", "https://github.com/patxibocos/poetimizely")

                val mitLicenseNode = root.appendNode("licenses").appendNode("license")
                mitLicenseNode.appendNode("name", "MIT License")
                mitLicenseNode.appendNode("url", "http://www.opensource.org/licenses/mit-license.php")

                val scmNode = root.appendNode("scm")
                scmNode.appendNode("connection", "scm:git:git://github.com/patxibocos/poetimizely.git")
                scmNode.appendNode("developerConnection", "scm:git:ssh://github.com:patxibocos/poetimizely.git")
                scmNode.appendNode("url", "http://github.com/patxibocos/poetimizely/tree/master")

                val developerNode = root.appendNode("developers").appendNode("developer")
                developerNode.appendNode("name", "Patxi Bocos")
                developerNode.appendNode("email", "patxi.bocos.vidal@gmail.com")
                developerNode.appendNode("url", "https://twitter.com/patxibocos")
                developerNode.appendNode("timezone", "Europe/Madrid")
            }
        }
    }
    repositories {
        mavenLocal()
    }
}
