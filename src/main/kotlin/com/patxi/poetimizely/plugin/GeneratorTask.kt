package com.patxi.poetimizely.plugin

import com.patxi.poetimizely.generator.Generator
import com.patxi.poetimizely.optimizely.ExperimentsService
import com.patxi.poetimizely.optimizely.buildExperimentsService
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class GeneratorTask @Inject constructor(
    private val optimizelyProjectId: Long,
    private val optimizelyToken: String
) : DefaultTask() {

    @TaskAction
    fun doAction() {
        val service: ExperimentsService = buildExperimentsService(optimizelyToken)
        val generator = Generator()
        runBlocking {
            val experiments = service.listExperiments(optimizelyProjectId)
            experiments.forEach {
                generator.buildExperimentObject(it)
            }
        }
    }
}
