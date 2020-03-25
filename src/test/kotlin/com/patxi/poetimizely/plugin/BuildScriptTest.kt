package com.patxi.poetimizely.plugin

import io.kotest.core.spec.style.WordSpec
import org.gradle.testkit.runner.GradleRunner
import java.nio.file.Files

class BuildScriptTest : WordSpec({

    "A build script configuring the plugin" should {
        "build correctly" {
            val projectDir = Files.createTempDirectory("")
            val buildScript = projectDir.resolve("build.gradle").toFile()
            buildScript.writeText(
                """
                plugins { 
                    id 'com.patxi.poetimizely'
                }
                
                poetimizely {
                    optimizelyProjectId = 111L
                    optimizelyToken = 'TOKEN'
                }
            """.trimIndent()
            )
            GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .build()
        }
    }
})
