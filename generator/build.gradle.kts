import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.patxi"
version = "1.0.0"

plugins {
    jacoco
    `maven-publish`
    kotlin("jvm") version "1.4.30"
    id("com.jfrog.bintray") version "1.8.5"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("com.optimizely.ab:core-api:3.8.0")
    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.3.4")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.4.0")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.4.0")
    testImplementation("io.mockk:mockk:1.10.5")
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

bintray {
    user = System.getenv("BINTRAY_API_USER")
    key = System.getenv("BINTRAY_API_KEY")
    publish = true
    setPublications("poetimizelyGenerator")
    pkg(delegateClosureOf<PackageConfig> {
        repo = project.group.toString()
        name = project.name
        desc = "poetimizely generator module that both Gradle and Maven plugins depend on"
        setLicenses("MIT")
        githubRepo = "patxibocos/poetimizely"
        vcsUrl = "https://github.com/patxibocos/poetimizely"
    })
}