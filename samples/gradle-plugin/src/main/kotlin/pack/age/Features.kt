package pack.age

import com.optimizely.ab.Optimizely
import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.collections.Map

public class FeatureVariable<T>(
  public val featureKey: String,
  public val variableKey: String
)

public sealed class Features(
  public val key: String
) {
  public object MyFeature : Features("my_feature")
}

public fun Optimizely.isFeatureEnabled(
  feature: Features,
  userId: String,
  attributes: Map<String, Any> = emptyMap()
): Boolean = this.isFeatureEnabled(feature.key, userId, attributes)

public fun Optimizely.getFeatureVariable(
  variable: FeatureVariable<Boolean>,
  userId: String,
  attributes: Map<String, Any> = emptyMap()
): Boolean? = this.getFeatureVariableBoolean(variable.featureKey, variable.variableKey, userId,
    attributes)

public fun Optimizely.getFeatureVariable(
  variable: FeatureVariable<String>,
  userId: String,
  attributes: Map<String, Any> = emptyMap()
): String? = this.getFeatureVariableString(variable.featureKey, variable.variableKey, userId,
    attributes)

public fun Optimizely.getFeatureVariable(
  variable: FeatureVariable<Double>,
  userId: String,
  attributes: Map<String, Any> = emptyMap()
): Double? = this.getFeatureVariableDouble(variable.featureKey, variable.variableKey, userId,
    attributes)

public fun Optimizely.getFeatureVariable(
  variable: FeatureVariable<Int>,
  userId: String,
  attributes: Map<String, Any> = emptyMap()
): Int? = this.getFeatureVariableInteger(variable.featureKey, variable.variableKey, userId,
    attributes)
