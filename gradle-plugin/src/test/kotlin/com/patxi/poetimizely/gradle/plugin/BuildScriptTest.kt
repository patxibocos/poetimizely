package com.patxi.poetimizely.gradle.plugin

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContainIgnoringCase
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.nio.file.Files

class BuildScriptTest : BehaviorSpec({

    given("A project directory") {
        val projectDir = Files.createTempDirectory("")
        val buildScript = projectDir.resolve("build.gradle.kts").toFile()
        and("A build script applying the plugin without configuration") {
            buildScript.writeText(
                """
                plugins { 
                    kotlin("jvm") version "1.3.72"
                    id("com.patxi.poetimizely")
                }
                """.trimIndent()
            )
            `when`("Poetimize task runs") {
                val buildResult = GradleRunner.create()
                    .withProjectDir(projectDir.toFile())
                    .withPluginClasspath()
                    .withArguments(":poetimize")
                    .build()
                then("Generator tasks runs but it skips as the configuration is missing") {
                    val poetimizeBuildTask = buildResult.tasks.find { it.path == ":poetimize" }
                    poetimizeBuildTask shouldNotBe null
                    poetimizeBuildTask?.outcome shouldBe TaskOutcome.SUCCESS
                    buildResult.output.shouldContainIgnoringCase("skipping")
                }
            }
        }
    }
})
