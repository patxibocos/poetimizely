package com.patxi.poetimizely.generator.optimizely

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal fun buildExperimentsService(retrofit: Retrofit): ExperimentsService =
    retrofit.create(ExperimentsService::class.java)

internal fun buildFeaturesService(retrofit: Retrofit): FeaturesService =
    retrofit.create(FeaturesService::class.java)

internal fun authenticatedRetrofit(optimizelyToken: String): Retrofit {
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
