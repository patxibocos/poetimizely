package com.patxi.poetimizely.optimizely

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun buildOptimizelyService(optimizelyToken: String): OptimizelyService {
    val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
        val newRequest = chain.request().newBuilder().addHeader("Authorization", "Bearer $optimizelyToken").build()
        chain.proceed(newRequest)
    }.build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.optimizely.com/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()

    return retrofit.create(OptimizelyService::class.java)
}

suspend fun listExperiments(projectId: Long, service: OptimizelyService): List<Experiment> =
    service.listExperiments(projectId)