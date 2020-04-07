[![codecov](https://codecov.io/gh/patxibocos/poetimizely/branch/master/graph/badge.svg)](https://codecov.io/gh/patxibocos/poetimizely)
[![CI](https://github.com/patxibocos/poetimizely/workflows/CI/badge.svg)](https://github.com/patxibocos/poetimizely/actions?query=workflow%3ACI)
[![gradle](https://img.shields.io/maven-metadata/v.svg?colorB=007ec6&label=gradle&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fcom%2Fpatxi%2Fpoetimizely%2Fcom.patxi.poetimizely.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/com.patxi.poetimizely)

## What is poetimizely ❓

**poetimizely** is a library to generate type safe accessors for [Optimizely](https://www.optimizely.com/) experiments.
Given a Project ID and a token it will generate classes for all the experiments and their variations.

## Gradle setup ⚙

There are just a few lines you need to add to your build Gradle file to use this plugin.

ℹ️ Before editing the Gradle file, there are three properties required to configure explained below:
- **optimizelyProjectId** (Long): id of the Optimizely project to grab experiments and features from. 
- **optimizelyToken** (String): Optimizely personal access token. See [Personal token authentication](https://docs.developers.optimizely.com/web/docs/personal-token).
- **packageName** (String): package where the code will be placed. The expected format is `your.destination.package`.

#### Kotlin DSL (build.gradle.kts)

```kotlin
plugins {
  id("com.patxi.poetimizely") version "1.0.0-beta01"
}

poetimizely {
    optimizelyProjectId = <OPTIMIZELY_PROJECT_ID> 
    optimizelyToken = <PERSONAL_ACCESS_TOKEN>
    packageName = <PACKAGE_NAME>
}
```

#### Groovy (build.gradle)

```groovy
plugins {
  id "com.patxi.poetimizely" version "1.0.0-beta01"
}

poetimizely {
    optimizelyProjectId = <OPTIMIZELY_PROJECT_ID> 
    optimizelyToken = <PERSONAL_ACCESS_TOKEN>
    packageName = <PACKAGE_NAME>
}

```

## Usage 📋

After the plugin has been setup, a new Gradle task named **poetimize** will be available. In order to run it successfully, both Optimizely project id and token must be valid.

```shell
./gradlew poetimize
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

Features is more simple, all of them are contained in a single enum class.

Another extension function is provided to query if a features is enabled:

```kotlin
if (optimizely.isFeatureEnabled(Features.EXAMPLE_FEATURE, userId)) {
    TODO()
}
```

## A look at the generated code 👀

After running the task there will be two new classes generated in the given **packageName**:

#### `Experiments.kt`

```kotlin
interface BaseVariation {
    val key: String
}

interface BaseExperiment<V : BaseVariation> {
    val key: String

    val variations: Array<V>
}

fun Optimizely.getAllExperiments(): List<BaseExperiment<out BaseVariation>> = 
    listOf(Experiments.ExampleExperiment)

fun <V : BaseVariation> Optimizely.getVariationForExperiment(
    experiment: BaseExperiment<out V>,
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

object Experiments {
    object ExampleExperiment : BaseExperiment<ExampleExperimentVariations> {
        override val key: String = "example-experiment"
        override val variations: Array<ExampleExperimentVariations> =
            ExampleExperimentVariations.values()
    }
}
```

#### `Features.kt`

```kotlin
enum class Features(
    val key: String
) {
    EXAMPLE_FEATURE("example-feature") // Your features will be here 
}

fun Optimizely.isFeatureEnabled(
    feature: Features,
    userId: String,
    attributes: Map<String, Any> = emptyMap()
): Boolean = this.isFeatureEnabled(feature.key, userId, attributes)
```
