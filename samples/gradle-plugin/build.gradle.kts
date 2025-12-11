group = "io.github.patxibocos"

plugins {
    application
    kotlin("jvm") version "2.2.21"
    id("io.github.patxibocos.poetimizely") version "1.0.8"
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("MainKt")
}

dependencies {
    implementation("com.optimizely.ab:core-api:4.3.0")
    implementation("com.optimizely.ab:core-httpclient-impl:4.2.2")
    implementation(kotlin("stdlib"))
}

poetimizely {
    optimizelyProjectId = System.getenv("OPTIMIZELY_PROJECT_ID").toLong()
    optimizelyToken = System.getenv("OPTIMIZELY_TOKEN")
    packageName = "pack.age"
}

tasks.getByName("build").finalizedBy("poetimize")
