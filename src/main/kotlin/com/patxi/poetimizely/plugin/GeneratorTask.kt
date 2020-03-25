package com.patxi.poetimizely.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class GeneratorTask @Inject constructor(
    val optimizelyProjectId: Long,
    val optimizelyToken: String
) : DefaultTask() {

    @TaskAction
    fun doAction() {
        println("optimizelyProjectId: $optimizelyProjectId")
        println("optimizelyToken: $optimizelyToken")
    }
}
