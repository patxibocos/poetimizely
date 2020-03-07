package com.patxi.poetimizely.generator

interface Experiment<V : Variant> {
    val key: String
    val variants: Array<V>
}