package com.patxi.poetimizely.generator.base

import com.optimizely.ab.Optimizely

abstract class BaseFeaturesClient<in F : BaseFeature>(private val optimizely: Optimizely, private val userId: String) {

    fun isFeatureEnabled(feature: F): Boolean {
        return optimizely.isFeatureEnabled(feature.key, userId)
    }
}
