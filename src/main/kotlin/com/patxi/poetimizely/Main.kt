@file:JvmName("Main")

package com.patxi.poetimizely

import com.patxi.poetimizely.optimizely.ListExperiments
import com.patxi.poetimizely.optimizely.OptimizelyService
import kotlinx.coroutines.runBlocking
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

fun main(args: Array<String>) {
    require(args.size == 2) { "2 arguments expected: --args=\"<optimizelyProjectId> <optimizelyToken>\"" }
    val (projectIdString, optimizelyToken) = args
    val projectId = requireNotNull(projectIdString.toLongOrNull()) { "optimizelyProjectId must be a number" }

    val service: OptimizelyService = buildOptimizelyService(optimizelyToken)

    runBlocking {
        val listExperiments = ListExperiments(service)
        val experiments = listExperiments(projectId = projectId)
        experiments.forEach(::println)
    }
}