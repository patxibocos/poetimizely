package com.patxi.poetimizely.optimizely

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun buildExperimentsService(optimizelyToken: String): ExperimentsService =
    authedRetrofit(optimizelyToken).create(ExperimentsService::class.java)

fun buildFeaturesService(optimizelyToken: String): FeaturesService =
    authedRetrofit(optimizelyToken).create(FeaturesService::class.java)

fun authedRetrofit(optimizelyToken: String): Retrofit {
    val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
        val newRequest = chain.request().newBuilder().addHeader("Authorization", "Bearer $optimizelyToken").build()
        chain.proceed(newRequest)
    }.build()

    return Retrofit.Builder()
        .baseUrl("https://api.optimizely.com/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()
}
