group = "io.github.patxibocos"
version = "1.0.5"

plugins {
    jacoco
    `maven-publish`
    signing
    id("com.diffplug.spotless") version "6.18.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.optimizely.ab:core-api:3.10.3")
    implementation("com.squareup:kotlinpoet:1.12.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    implementation("io.ktor:ktor-client-core:2.2.4")
    implementation("io.ktor:ktor-client-content-negotiation:2.2.4")
    implementation("io.ktor:ktor-client-cio:2.2.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.4")
    implementation("com.pinterest.ktlint:ktlint-core:0.48.2")
    implementation("com.pinterest.ktlint:ktlint-ruleset-standard:0.48.2")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.5")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.5")
    testImplementation("io.mockk:mockk:1.13.4")
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

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "io.github.patxibocos"
                artifactId = "poetimizely-core"
                version = project.version.toString()
                description = "poetimizely core module that both Gradle and Maven plugins depend on"

                from(components["java"])

                pom {
                    name.set("poetimizely-core")
                    description.set("poetimizely core module that both Gradle and Maven plugins depend on")
                    url.set("https://github.com/patxibocos/poetimizely")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/mit-license.php")
                        }
                    }
                    developers {
                        developer {
                            id.set("patxibocos")
                            name.set("Patxi Bocos")
                            email.set("patxi.bocos.vidal@gmail.com")
                            url.set("https://twitter.com/patxibocos")
                            timezone.set("Europe/Madrid")
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/patxibocos/poetimizely.git")
                        developerConnection.set("scm:git:ssh://github.com/patxibocos/poetimizely.git")
                        url.set("https://github.com/patxibocos/poetimizely/tree/main")
                    }
                }
            }
        }
    }

    if (properties["skipSigning"] == null) {
        signing {
            val signingKeyId: String? by project
            val signingSecretKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKeyId, signingSecretKey, signingPassword)
            sign(publishing.publications)
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            val sonatypeStagingProfileId: String? by project
            val sonatypeUsername: String? by project
            val sonatypePassword: String? by project
            stagingProfileId.set(sonatypeStagingProfileId)
            username.set(sonatypeUsername)
            password.set(sonatypePassword)
            // only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
