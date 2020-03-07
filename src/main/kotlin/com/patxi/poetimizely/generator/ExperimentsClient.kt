package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely

class ExperimentsClient(private val optimizely: Optimizely, private val userId: String) {
    fun <V : Variant> getVariantForExperiment(experiment: Experiment<V>): V? {
        val variation = optimizely.activate(experiment.key, userId)
        return experiment.variants.find { it.key == variation?.key }
    }

    fun getExperiments(): List<Experiment<*>> {
        return emptyList()
    }
}