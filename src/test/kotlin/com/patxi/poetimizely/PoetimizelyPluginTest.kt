package com.patxi.poetimizely

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldNotBe
import org.gradle.testfixtures.ProjectBuilder

class PoetimizelyPluginTest : WordSpec({

    "A Gradle project with the plugin applied" should {
        "Contain the plugin" {
            val project = ProjectBuilder.builder().build()

            project.pluginManager.apply("com.patxi.poetimizely")

            project.plugins.getPlugin(PoetimizelyPlugin::class.java) shouldNotBe null
        }
    }
})
