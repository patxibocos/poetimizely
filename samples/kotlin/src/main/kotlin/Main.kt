import com.optimizely.ab.Optimizely
import com.optimizely.ab.OptimizelyFactory
import what.ever.pack.age.Features
import what.ever.pack.age.FeaturesClient

fun main() {
    val optimizely = buildOptimizely()
    val featuresClient = FeaturesClient(optimizely, "userId")

    val isMyFeatureEnabled = featuresClient.isFeatureEnabled(Features.MY_FEATURE)
    println("${Features.MY_FEATURE.key} enabled: $isMyFeatureEnabled")
}

private fun buildOptimizely(): Optimizely =
    OptimizelyFactory.newDefaultInstance(System.getenv("OPTIMIZELY_SDK_KEY"))
