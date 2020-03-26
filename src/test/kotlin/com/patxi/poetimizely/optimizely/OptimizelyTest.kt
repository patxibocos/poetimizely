package com.patxi.poetimizely.optimizely

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import okhttp3.Request

class OptimizelyTest : BehaviorSpec({
    given("An Optimizely token") {
        val optimizelyToken = "token"
        and("A Retrofit instance") {
            val authedRetrofit = authedRetrofit(optimizelyToken)
            `when`("Making a request using the Retrofit instance") {
                val request = authedRetrofit.callFactory()
                    .newCall(Request.Builder().url("https://github.com/patxibocos/poetimizely").build())
                val response = request.execute()
                then("Original request should contain the Optimizely token in the headers") {
                    response.request().header("Authorization") shouldBe "Bearer $optimizelyToken"
                    authedRetrofit.baseUrl().host() shouldBe "api.optimizely.com"
                }
            }
        }
    }
})
