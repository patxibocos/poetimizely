package com.patxi.poetimizely.generator.base

interface Experiment<V : Variant> {
    val key: String
    val variants: Array<V>
}
