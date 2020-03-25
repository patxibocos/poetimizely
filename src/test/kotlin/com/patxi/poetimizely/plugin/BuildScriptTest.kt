package com.patxi.poetimizely.plugin

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.nio.file.Files

class BuildScriptTest : WordSpec({

    "Whatever" should {
        "build" {
            val projectDir = Files.createTempDirectory("")
            val buildScript = projectDir.resolve("build.gradle").toFile()
            buildScript.writeText(
                """
                plugins { 
                    id 'com.patxi.poetimizely'
                }
                
                poetimizely {
                    optimizelyProjectId = 1L
                    optimizelyToken = 'token'
                }
            """.trimIndent()
            )
            val result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments("poetimize", "--info", "--stacktrace")
                .build()

            val poetimizeTaskResult = result.task(":poetimize")
            poetimizeTaskResult shouldNotBe null
            poetimizeTaskResult?.outcome shouldBe TaskOutcome.SUCCESS
        }
    }
})