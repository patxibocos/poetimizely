[![codecov](https://codecov.io/gh/patxibocos/poetimizely/branch/master/graph/badge.svg)](https://codecov.io/gh/patxibocos/poetimizely)
[![CI](https://github.com/patxibocos/poetimizely/workflows/CI/badge.svg)](https://github.com/patxibocos/poetimizely/actions?query=workflow%3ACI)
[![generator](https://img.shields.io/bintray/v/patxibocos/com.patxi/poetimizely-generator?label=generator)](https://bintray.com/patxibocos/com.patxi/poetimizely-generator)
[![gradle-plugin](https://img.shields.io/maven-metadata/v.svg?colorB=007ec6&label=gradle-plugin&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fcom%2Fpatxi%2Fpoetimizely%2Fcom.patxi.poetimizely.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/com.patxi.poetimizely)
[![maven-plugin](https://img.shields.io/bintray/v/patxibocos/com.patxi/poetimizely-maven-plugin?label=maven-plugin)](https://bintray.com/patxibocos/com.patxi/poetimizely-maven-plugin)

## What is poetimizely ❓

**poetimizely** is a library to generate type safe accessors for [Optimizely](https://www.optimizely.com/) experiments and features.
Given a Project ID and a token it will generate classes for every experiment + variations and features + variables.

## Setup ⚙

ℹ️ Before editing the build file, there are three properties required to configure explained below:
- **optimizelyProjectId** (Long): id of the Optimizely project to grab experiments and features from. 
- **optimizelyToken** (String): Optimizely personal access token. See [Personal token authentication](https://docs.developers.optimizely.com/web/docs/personal-token).
- **packageName** (String): package where the code will be placed. The expected format is `your.destination.package`.

### Gradle 🐘

#### Kotlin DSL (build.gradle.kts)

```kotlin
plugins {
  id("com.patxi.poetimizely") version "1.0.0"
}

poetimizely {
    optimizelyProjectId = $OPTIMIZELY_PROJECT_ID
    optimizelyToken = $PERSONAL_ACCESS_TOKEN
    packageName = $PACKAGE_NAME
}
```

#### Groovy (build.gradle)

```groovy
plugins {
  id "com.patxi.poetimizely" version "1.0.0"
}

poetimizely {
    optimizelyProjectId = $OPTIMIZELY_PROJECT_ID
    optimizelyToken = $PERSONAL_ACCESS_TOKEN
    packageName = $PACKAGE_NAME
}

```

### Maven 🕊️

#### pom.xml

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.patxi</groupId>
            <artifactId>poetimizely-maven-plugin</artifactId>
            <version>1.0.0</version>
            <configuration>
                <optimizelyProjectId>$OPTIMIZELY_PROJECT_ID</optimizelyProjectId>
                <optimizelyToken>$PERSONAL_ACCESS_TOKEN</optimizelyToken>
                <packageName>$PACKAGE_NAME</packageName>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Usage 📋

After the plugin has been setup, a new Gradle task / Maven goal named **poetimize** will be available. In order to run it successfully, both Optimizely project id and token must be valid.

For Gradle projects run:

```shell
./gradlew poetimize
```

or with Maven:

```shell
./mvnw poetimizely:poetimize
```

This will generate all the code based on the experiments (and its variants) and features defined for the given Optimizely project.

### Experiments 🧪

For each of the experiments, a new [object](https://kotlinlang.org/docs/reference/object-declarations.html#object-declarations) will be generated. And for the set of variations for each of the experiments an enum class will be also created.

An extension function for the Optimizely class is also available that brings the **type safety**:

```kotlin
when (optimizely.getVariationForExperiment(Experiments.ExampleExperiment, userId)) {
    EXAMPLE_VARIATION -> TODO() 
    null -> TODO()
}
```

### Features 💡

Kotlin objects will also be generated for features. For the set of variables that each of the features may have, a property is added to the object.

To check whether a feature is enabled an extension function is provided:

```kotlin
if (optimizely.isFeatureEnabled(Features.ExampleFeature, userId)) {
    TODO()
}
```

and also for getting feature variables values:

```kotlin
val booleanVariable: Boolean? = optimizely.getFeatureVariable(Features.ExampleFeature.exampleBooleanVariable)
val stringVariable: String? = optimizely.getFeatureVariable(Features.ExampleFeature.exampleStringVariable)
val doubleVariable: Double? = optimizely.getFeatureVariable(Features.ExampleFeature.exampleDoubleVariable)
val intVariable: Int? = optimizely.getFeatureVariable(Features.ExampleFeature.exampleIntVariable)
```

## A look at the generated code 👀

After running the task there will be two new classes generated in the given **packageName**:

#### `Experiments.kt`

```kotlin
interface BaseVariation {
    val key: String
}

fun Optimizely.getAllExperiments(): List<Experiments<out BaseVariation>> =
    listOf(Experiments.ExampleExperiment)

fun <V : BaseVariation> Optimizely.getVariationForExperiment(
    experiment: Experiments<out V>,
    userId: String,
    attributes: Map<String, Any> = emptyMap()
): V? {
    val variation = this.activate(experiment.key, userId, attributes)
    return experiment.variations.find { it.key == variation?.key }
}

enum class ExampleExperimentVariations : BaseVariation {
    EXAMPLE_VARIATION {
        override val key: String = "example-variation"
    }
}

sealed class Experiments<V : BaseVariation>(
    val key: String,
    val variations: Array<V>
) {
    object ExampleExperiment : Experiments<ExampleExperimentVariations> (
        "example-experiment",
        ExampleExperimentVariations.values()
    )
}
```

#### `Features.kt`

```kotlin
class FeatureVariable<T>(
    val featureKey: String,
    val variableKey: String
)

sealed class Features(
    val key: String
) {
    object ExampleFeature("example-feature") {
        val exampleVariable: FeatureVariable<Boolean> = FeatureVariable("example-feature", "example-variable")
    } 
}

fun Optimizely.isFeatureEnabled(
    feature: Features,
    userId: String,
    attributes: Map<String, Any> = emptyMap()
): Boolean = this.isFeatureEnabled(feature.key, userId, attributes)

fun Optimizely.getFeatureVariable(
    variable: FeatureVariable<Boolean>,
    userId: String,
    attributes: Map<String, Any> = emptyMap()
): Boolean? =
    this.getFeatureVariableBoolean(variable.featureKey, variable.variableKey, userId, attributes)

fun Optimizely.getFeatureVariable(
    variable: FeatureVariable<String>,
    userId: String,
    attributes: Map<String, Any> = emptyMap()
): String? =
    this.getFeatureVariableString(variable.featureKey, variable.variableKey, userId, attributes)

fun Optimizely.getFeatureVariable(
    variable: FeatureVariable<Double>,
    userId: String,
    attributes: Map<String, Any> = emptyMap()
): Double? =
    this.getFeatureVariableDouble(variable.featureKey, variable.variableKey, userId, attributes)

fun Optimizely.getFeatureVariable(
    variable: FeatureVariable<Int>,
    userId: String,
    attributes: Map<String, Any> = emptyMap()
): Int? =
    this.getFeatureVariableInteger(variable.featureKey, variable.variableKey, userId, attributes)
```
