package com.patxi.poetimizely.plugin

import com.patxi.poetimizely.generator.ExperimentsGenerator
import com.patxi.poetimizely.optimizely.ExperimentsService
import com.patxi.poetimizely.optimizely.authenticatedRetrofit
import com.patxi.poetimizely.optimizely.buildExperimentsService
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class GeneratorTask : DefaultTask() {
    var optimizelyProjectId: Long? = null
    var optimizelyToken: String? = null

    @TaskAction
    fun doAction() {
        val optimizelyProjectId = optimizelyProjectId
        val optimizelyToken = optimizelyToken
        if (optimizelyProjectId == null || optimizelyToken == null) {
            logger.error("Skipping generator task as missing required arguments")
            return
        }
        val authenticatedRetrofit = authenticatedRetrofit(optimizelyToken)
        val service: ExperimentsService = buildExperimentsService(authenticatedRetrofit)
        val experimentsGenerator = ExperimentsGenerator()
        runBlocking {
            val experiments = service.listExperiments(optimizelyProjectId)
            experimentsGenerator.build(experiments)
        }
    }
}
