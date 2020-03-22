@file:JvmName("Main")

package com.patxi.poetimizely

import com.patxi.poetimizely.generator.Generator
import com.patxi.poetimizely.optimizely.OptimizelyService
import com.patxi.poetimizely.optimizely.buildOptimizelyService
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    require(args.size == 2) { "2 arguments expected: --args=\"<optimizelyProjectId> <optimizelyToken>\"" }
    val (projectIdString, optimizelyToken) = args
    val projectId = requireNotNull(projectIdString.toLongOrNull()) { "optimizelyProjectId must be a number" }

    val service: OptimizelyService = buildOptimizelyService(optimizelyToken)
    val generator = Generator()
    runBlocking {
        val experiments = service.listExperiments(projectId)
        experiments.forEach {
            generator.buildExperimentObject(it)
        }
    }
}
