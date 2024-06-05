package io.github.patxibocos.poetimizely.core.optimizely

import kotlinx.serialization.Serializable

@Serializable
internal data class Experiment(
    val key: String,
    val variations: List<Variation>,
)

@Serializable
internal data class Variation(
    val key: String,
)

internal interface ExperimentsService {
    suspend fun listExperiments(projectId: Long): List<Experiment>
}

@Serializable
internal data class Feature(
    val key: String,
    val variables: Collection<Variable> = emptyList(),
)

@Serializable
internal data class Variable(
    val key: String,
    val type: String,
)

internal interface FeaturesService {
    suspend fun listFeatures(projectId: Long): List<Feature>
}
