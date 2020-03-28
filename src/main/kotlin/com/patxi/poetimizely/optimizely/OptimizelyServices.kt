package com.patxi.poetimizely.optimizely

import retrofit2.http.GET
import retrofit2.http.Query

data class OptimizelyExperiment(val key: String, val variations: List<OptimizelyVariation>)
data class OptimizelyVariation(val key: String)

interface ExperimentsService {
    @GET("experiments")
    suspend fun listExperiments(@Query("project_id") projectId: Long): List<OptimizelyExperiment>
}

data class Feature(val key: String)

interface FeaturesService {
    @GET("features")
    suspend fun listFeatures(@Query("project_id") projectId: Long): List<Feature>
}
