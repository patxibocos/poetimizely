package com.patxi.poetimizely.plugin

import com.patxi.poetimizely.generator.Generator
import com.patxi.poetimizely.optimizely.ExperimentsService
import com.patxi.poetimizely.optimizely.buildExperimentsService
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class GeneratorTask : DefaultTask() {

    private val optimizelyProjectId = project.poetimizely().optimizelyProjectId
    private val optimizelyToken = project.poetimizely().optimizelyToken

    @TaskAction
    fun doAction() {
        val service: ExperimentsService = buildExperimentsService(requireNotNull(optimizelyToken))
        val generator = Generator()
        runBlocking {
            val experiments = service.listExperiments(requireNotNull(optimizelyProjectId))
            experiments.forEach {
                generator.buildExperimentObject(it)
            }
        }
    }
}
