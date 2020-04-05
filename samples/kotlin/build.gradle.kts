plugins {
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
}

poetimizely {
    optimizelyProjectId = System.getenv("OPTIMIZELY_PROJECT_ID").toLong()
    optimizelyToken = System.getenv("OPTIMIZELY_TOKEN")
    packageName = "what.ever.pack.age"
}

tasks.getByName("build").finalizedBy("poetimize")