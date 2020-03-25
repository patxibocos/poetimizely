package com.patxi.poetimizely.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

private const val POETIMIZELY_EXTENSION_NAME = "poetimizely"

class PoetimizelyPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(POETIMIZELY_EXTENSION_NAME, PoetimizelyExtension::class.java)
        project.tasks.register("poetimize", GeneratorTask::class.java) { task: GeneratorTask ->
            task.optimizelyProjectId = extension.optimizelyProjectId
            task.optimizelyToken = extension.optimizelyToken
        }
    }
}

internal fun Project.poetimizely(): PoetimizelyExtension =
    extensions.getByName(POETIMIZELY_EXTENSION_NAME) as? PoetimizelyExtension
        ?: throw IllegalStateException("$POETIMIZELY_EXTENSION_NAME is not of the correct type")
