package com.patxi.poetimizely.generator.base

interface BaseExperiment<V : BaseVariant> {
    val key: String
    val variants: Array<V>
}
