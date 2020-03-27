package com.patxi.poetimizely.generator.base

import com.optimizely.ab.Optimizely

abstract class FeaturesClient<in F : Feature>(private val optimizely: Optimizely, private val userId: String) {

    fun isFeatureEnabled(feature: F): Boolean {
        return optimizely.isFeatureEnabled(feature.key, userId)
    }
}
