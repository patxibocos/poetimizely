@file:JvmName("Api")

package io.github.patxibocos.poetimizely.generator

import io.github.patxibocos.poetimizely.generator.optimizely.authenticatedRetrofit
import io.github.patxibocos.poetimizely.generator.optimizely.buildExperimentsService
import io.github.patxibocos.poetimizely.generator.optimizely.buildFeaturesService

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
