package what.ever.pack.age

import com.optimizely.ab.Optimizely

enum class Features(
    val key: String
) {
    MY_FEATURE("my_feature");
}

class FeaturesClient(
    private val optimizely: Optimizely,
    private val userId: String
) {
    fun isFeatureEnabled(feature: Features): Boolean = optimizely.isFeatureEnabled(
        feature.key,
        userId
    )
}
