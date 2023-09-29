@file:JvmName("Api")
@file:Suppress("Unused")

package io.github.patxibocos.poetimizely.core

import io.github.patxibocos.poetimizely.core.generator.generateExperimentsCode
import io.github.patxibocos.poetimizely.core.generator.generateFeaturesCode
import io.github.patxibocos.poetimizely.core.optimizely.authenticatedHttpClient
import io.github.patxibocos.poetimizely.core.optimizely.buildExperimentsService
import io.github.patxibocos.poetimizely.core.optimizely.buildFeaturesService

suspend fun codeForExperiments(
    optimizelyProjectId: Long,
    optimizelyToken: String,
    packageName: String,
): String {
    val httpClient = authenticatedHttpClient(optimizelyToken)
    val experimentsService = buildExperimentsService(httpClient)
    val experiments = experimentsService.listExperiments(optimizelyProjectId)
    return generateExperimentsCode(experiments, packageName)
}

suspend fun codeForFeatures(
    optimizelyProjectId: Long,
    optimizelyToken: String,
    packageName: String,
): String {
    val httpClient = authenticatedHttpClient(optimizelyToken)
    val featuresService = buildFeaturesService(httpClient)
    val features = featuresService.listFeatures(optimizelyProjectId)
    return generateFeaturesCode(features, packageName)
}
