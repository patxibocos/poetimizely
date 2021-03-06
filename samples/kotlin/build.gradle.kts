plugins {
    application
    kotlin("jvm") version "1.4.20"
    id("com.patxi.poetimizely") version "1.0.0"
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("com.optimizely.ab:core-api:3.7.0")
    implementation("com.optimizely.ab:core-httpclient-impl:3.4.3")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")

    implementation("com.fasterxml.jackson.core:jackson-core:2.11.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.11.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.3")

    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")
}

application {
    mainClassName = "MainKt"
}

poetimizely {
    optimizelyProjectId = System.getenv("OPTIMIZELY_PROJECT_ID").toLong()
    optimizelyToken = System.getenv("OPTIMIZELY_TOKEN")
    packageName = "what.ever.pack.age"
}

tasks.getByName("build").finalizedBy("poetimize")