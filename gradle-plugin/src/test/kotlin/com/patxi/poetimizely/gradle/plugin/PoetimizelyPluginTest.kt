package com.patxi.poetimizely.gradle.plugin

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testfixtures.ProjectBuilder

class PoetimizelyPluginTest : BehaviorSpec({

    given("A Gradle project") {
        val project = ProjectBuilder.builder().build()
        `when`("Poetimizely plugin is applied") {
            project.pluginManager.apply("com.patxi.poetimizely")
            then("Plugin is contained") {
                project.plugins.getPlugin(PoetimizelyPlugin::class.java) shouldNotBe null
            }
            then("Extension is contained") {
                project.extensions.getByName("poetimizely").shouldBeInstanceOf<PoetimizelyExtension>()
            }
            then("Task is contained") {
                val generatorTask = project.getTasksByName("poetimize", false)
                generatorTask.shouldBeSingleton()
                generatorTask.first().shouldBeInstanceOf<GeneratorTask>()
            }
        }
    }
})
