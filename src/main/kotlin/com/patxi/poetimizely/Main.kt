@file:JvmName("Main")

package com.patxi.poetimizely

import com.patxi.poetimizely.optimizely.ListExperiments
import com.patxi.poetimizely.optimizely.OptimizelyService
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun main(args: Array<String>) {
    val projectId = args[0].toLong()
    val optimizelyToken = args[1]
    val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
        val newRequest = chain.request().newBuilder().addHeader("Authorization", "Bearer $optimizelyToken").build()
        chain.proceed(newRequest)
    }.build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.optimizely.com/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()

    val service: OptimizelyService = retrofit.create(OptimizelyService::class.java)

    runBlocking {
        val experiments = ListExperiments(service)(projectId)
        experiments.forEach(::println)
    }
}