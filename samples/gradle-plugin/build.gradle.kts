group = "io.github.patxibocos"

plugins {
    application
    kotlin("jvm") version "1.8.22"
    id("io.github.patxibocos.poetimizely") version "1.0.5"
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("MainKt")
}

dependencies {
    implementation("com.optimizely.ab:core-api:3.10.3")
    implementation("com.optimizely.ab:core-httpclient-impl:3.10.3")
    implementation(kotlin("stdlib"))
}

poetimizely {
    optimizelyProjectId = System.getenv("OPTIMIZELY_PROJECT_ID").toLong()
    optimizelyToken = System.getenv("OPTIMIZELY_TOKEN")
    packageName = "pack.age"
}

tasks.getByName("build").finalizedBy("poetimize")