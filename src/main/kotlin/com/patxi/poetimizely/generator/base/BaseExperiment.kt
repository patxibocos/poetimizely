package com.patxi.poetimizely.generator.base

interface BaseExperiment<V : BaseVariation> {
    val key: String
    val variations: Array<V>
}
