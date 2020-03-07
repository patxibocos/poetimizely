package com.patxi.poetimizely.optimizely

class ListExperiments(private val optimizelyService: OptimizelyService) {

    suspend operator fun invoke(projectId: Long): List<Experiment> =
        optimizelyService.listExperiments(projectId)

}