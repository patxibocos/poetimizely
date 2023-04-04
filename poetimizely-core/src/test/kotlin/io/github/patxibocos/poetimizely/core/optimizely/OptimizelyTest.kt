package io.github.patxibocos.poetimizely.core.optimizely

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.statement.request

class OptimizelyTest : BehaviorSpec({
    given("An Optimizely token") {
        val optimizelyToken = "token"
        and("An HttpClient") {
            val httpClient = authenticatedHttpClient(optimizelyToken)
            `when`("Making a request") {
                val response = httpClient.get("")
                then("Original request should contain the Optimizely token in the headers") {
                    response.request.headers["Authorization"] shouldBe "Bearer $optimizelyToken"
                    response.request.url.host shouldBe "api.optimizely.com"
                }
            }
        }
    }
})
