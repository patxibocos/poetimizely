package what.ever.pack.age

import com.optimizely.ab.Optimizely

interface BaseVariation {
    val key: String
}

interface BaseExperiment<V : BaseVariation> {
    val key: String

    val variations: Array<V>
}

class ExperimentsClient(
    private val optimizely: Optimizely,
    private val userId: String
) {
    fun getAllExperiments(): List<BaseExperiment<out BaseVariation>> = listOf()

    fun <V : BaseVariation> getVariationForExperiment(
        experiment: BaseExperiment<out V>,
        attributes: Map<String, Any> = emptyMap()
    ): V? {
        val variation = optimizely.activate(experiment.key, userId, attributes)
        return experiment.variations.find { it.key == variation?.key }
    }
}
