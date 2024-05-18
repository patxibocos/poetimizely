@file:Suppress("RedundantVisibilityModifier", "Unused")

package pack.age

import com.optimizely.ab.Optimizely
import kotlin.Any
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map

public interface BaseVariation {
    public val key: String
}

public fun getAllExperiments(): List<Experiments<out BaseVariation>> = listOf(Experiments.MyFeatureTest)

public fun <V : BaseVariation> Optimizely.getVariationForExperiment(
    experiment: Experiments<V>,
    userId: String,
    attributes: Map<String, Any> = emptyMap(),
): V? {
    val variation = this.activate(experiment.key, userId, attributes)
    return experiment.variations.find { it.key == variation?.key }
}

public enum class MyFeatureTestVariations : BaseVariation {
    VARIATION_1 {
        override val key: String = "variation_1"
    },
    VARIATION_2 {
        override val key: String = "variation_2"
    },
    VARIATION_3 {
        override val key: String = "variation_3"
    },
}

public sealed class Experiments<V : BaseVariation>(
    public val key: String,
    public val variations: List<V>,
) {
    public data object MyFeatureTest : Experiments<MyFeatureTestVariations>(
        "my_feature_test",
        pack.age.MyFeatureTestVariations.entries,
    )
}
