import com.optimizely.ab.Optimizely
import com.optimizely.ab.OptimizelyFactory
import pack.age.Experiments
import pack.age.Features
import pack.age.getAllExperiments
import pack.age.getAllFeatures
import pack.age.getVariationForExperiment
import pack.age.isFeatureEnabled

fun main() {
    val optimizely = buildOptimizely()
    // ℹ️ After the code is generated by poetimizely, all of this is now possible:
    getAllExperiments().forEach(::println)
    getAllFeatures().forEach(::println)
    optimizely.isFeatureEnabled(Features.MyFeature, "")
    optimizely.getVariationForExperiment(Experiments.MyFeatureTest, "")
}

private fun buildOptimizely(): Optimizely =
    OptimizelyFactory.newDefaultInstance(System.getenv("OPTIMIZELY_SDK_KEY"))
