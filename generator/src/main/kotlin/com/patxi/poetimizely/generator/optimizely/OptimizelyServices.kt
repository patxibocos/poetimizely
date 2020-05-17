package com.patxi.poetimizely.generator.optimizely

import retrofit2.http.GET
import retrofit2.http.Query

internal data class OptimizelyExperiment(val key: String, val variations: List<OptimizelyVariation>)
internal data class OptimizelyVariation(val key: String)

internal interface ExperimentsService {
    @GET("experiments")
    suspend fun listExperiments(@Query("project_id") projectId: Long): List<OptimizelyExperiment>
}

internal data class Feature(val key: String, val variables: Collection<Variable> = emptyList())
internal data class Variable(val key: String, val type: String)

internal interface FeaturesService {
    @GET("features")
    suspend fun listFeatures(@Query("project_id") projectId: Long): List<Feature>
}
