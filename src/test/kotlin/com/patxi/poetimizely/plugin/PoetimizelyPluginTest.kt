package com.patxi.poetimizely.plugin

import io.kotest.core.spec.style.BehaviorSpec
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
                project.poetimizely() shouldNotBe null
            }
            then("Task is contained") {
                project.tasks.getByName("poetimize").shouldBeInstanceOf<GeneratorTask>()
            }
        }
    }
})
