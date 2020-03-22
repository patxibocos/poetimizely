package com.patxi.poetimizely.generator.base

import com.optimizely.ab.Optimizely

abstract class ExperimentsClient(private val optimizely: Optimizely, private val userId: String) {

    @JvmOverloads
    fun <V : Variant> getVariantForExperiment(
        experiment: Experiment<V>,
        attributes: Map<String, *> = emptyMap<String, String>()
    ): V? {
        val variation = optimizely.activate(experiment.key, userId, attributes)
        return experiment.variants.find { it.key == variation?.key }
    }

    abstract fun getAllExperiments(): List<Experiment<out Variant>>
}
