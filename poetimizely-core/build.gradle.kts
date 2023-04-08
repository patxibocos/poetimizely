group = "io.github.patxibocos"
version = "1.0.5"

plugins {
    jacoco
    `maven-publish`
    signing
    alias(libs.plugins.spotless)
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.optimizely.core.api)
    implementation(libs.kotlinpoet)
    api(libs.kotlin.coroutines.core.jvm)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.serialization.json)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.ktlint.core)
    implementation(libs.ktlint.ruleset.standard)

    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core.jvm)
    testImplementation(libs.mockk)
    testImplementation(gradleTestKit())
}

kotlin {
    jvmToolchain(11)
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("$buildDir/**/*.kt", "bin/**/*.kt")
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
