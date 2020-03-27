package com.patxi.poetimizely.generator.base

import com.optimizely.ab.Optimizely

abstract class BaseExperimentsClient(private val optimizely: Optimizely, private val userId: String) {

    @JvmOverloads
    fun <V : BaseVariant> getVariantForExperiment(
        baseExperiment: BaseExperiment<V>,
        attributes: Map<String, *> = emptyMap<String, String>()
    ): V? {
        val variation = optimizely.activate(baseExperiment.key, userId, attributes)
        return baseExperiment.variants.find { it.key == variation?.key }
    }

    abstract fun getAllExperiments(): List<BaseExperiment<out BaseVariant>>
}
