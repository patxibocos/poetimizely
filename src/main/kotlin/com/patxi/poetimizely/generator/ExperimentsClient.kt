package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely

class ExperimentsClient(private val optimizely: Optimizely, private val userId: String) {

    @JvmOverloads
    fun <V : Variant> getVariantForExperiment(
        experiment: Experiment<V>,
        attributes: Map<String, *> = emptyMap<String, String>()
    ): V? {
        val variation = optimizely.activate(experiment.key, userId, attributes)
        return experiment.variants.find { it.key == variation?.key }
    }

    fun getExperiments(): List<Experiment<*>> {
        return emptyList()
    }
}