plugins {
    application
    kotlin("jvm") version "1.3.71"
    id("com.patxi.poetimizely") version "1.0.0-alpha02"
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.optimizely.ab:core-api:3.4.2")
    implementation("com.optimizely.ab:core-httpclient-impl:3.3.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.6")

    implementation("com.fasterxml.jackson.core:jackson-core:2.9.8")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.9.8")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")

    implementation("org.slf4j:slf4j-api:1.7.16")
    implementation("ch.qos.logback:logback-classic:1.1.7")
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