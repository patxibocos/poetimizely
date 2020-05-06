@file:JvmName("Api")

package com.patxi.poetimizely.generator

import com.patxi.poetimizely.generator.optimizely.authenticatedRetrofit
import com.patxi.poetimizely.generator.optimizely.buildExperimentsService
import com.patxi.poetimizely.generator.optimizely.buildFeaturesService

suspend fun codeForExperiments(optimizelyProjectId: Long, optimizelyToken: String, packageName: String): String {
    val authenticatedRetrofit = authenticatedRetrofit(optimizelyToken)
    val experimentsService = buildExperimentsService(authenticatedRetrofit)
    val experiments = experimentsService.listExperiments(optimizelyProjectId)
    return generateExperimentsCode(experiments, packageName)
}

suspend fun codeForFeatures(optimizelyProjectId: Long, optimizelyToken: String, packageName: String): String {
    val authenticatedRetrofit = authenticatedRetrofit(optimizelyToken)
    val featuresService = buildFeaturesService(authenticatedRetrofit)
    val features = featuresService.listFeatures(optimizelyProjectId)
    return generateFeaturesCode(features, packageName)
}
