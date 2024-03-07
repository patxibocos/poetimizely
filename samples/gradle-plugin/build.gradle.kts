group = "io.github.patxibocos"

plugins {
    application
    kotlin("jvm") version "1.9.23"
    id("io.github.patxibocos.poetimizely") version "1.0.6"
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("MainKt")
}

dependencies {
    implementation("com.optimizely.ab:core-api:4.0.0")
    implementation("com.optimizely.ab:core-httpclient-impl:4.0.0")
    implementation(kotlin("stdlib"))
}

poetimizely {
    optimizelyProjectId = System.getenv("OPTIMIZELY_PROJECT_ID").toLong()
    optimizelyToken = System.getenv("OPTIMIZELY_TOKEN")
    packageName = "pack.age"
}

tasks.getByName("build").finalizedBy("poetimize")