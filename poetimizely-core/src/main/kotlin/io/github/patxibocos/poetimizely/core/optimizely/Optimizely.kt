package io.github.patxibocos.poetimizely.core.optimizely

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun buildExperimentsService(httpClient: HttpClient): ExperimentsService =
    object : ExperimentsService {
        override suspend fun listExperiments(projectId: Long): List<Experiment> {
            return httpClient.get("experiments") {
                url {
                    parameters.append("project_id", projectId.toString())
                }
            }.body()
        }
    }

internal fun buildFeaturesService(httpClient: HttpClient): FeaturesService =
    object : FeaturesService {
        override suspend fun listFeatures(projectId: Long): List<Feature> {
            return httpClient.get("features") {
                url {
                    parameters.append("project_id", projectId.toString())
                }
            }.body()
        }
    }

internal fun authenticatedHttpClient(optimizelyToken: String): HttpClient {
    return HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                },
            )
        }
        defaultRequest {
            url("https://api.optimizely.com/v2/")
            header("Authorization", "Bearer $optimizelyToken")
        }
    }
}
