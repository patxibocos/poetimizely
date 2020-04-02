package com.patxi.poetimizely.plugin

import com.patxi.poetimizely.generator.ExperimentsGenerator
import com.patxi.poetimizely.generator.FeaturesGenerator
import com.patxi.poetimizely.optimizely.ExperimentsService
import com.patxi.poetimizely.optimizely.FeaturesService
import com.patxi.poetimizely.optimizely.authenticatedRetrofit
import com.patxi.poetimizely.optimizely.buildExperimentsService
import com.patxi.poetimizely.optimizely.buildFeaturesService
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GeneratorTask : DefaultTask() {
    var optimizelyProjectId: Long? = null
    var optimizelyToken: String? = null
    var packageName: String? = null

    @TaskAction
    fun doAction() {
        val mainSrcDirs =
            project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName("main").allSource.srcDirs
        val targetDir =
            requireNotNull(mainSrcDirs.find { it.name == "kotlin" } ?: mainSrcDirs.find { it.name == "java" }) {
                "Cannot find a source directory for Java/Kotlin"
            }
        val optimizelyProjectId = optimizelyProjectId
        val optimizelyToken = optimizelyToken
        val packageName = packageName
        if (optimizelyProjectId == null || optimizelyToken == null || packageName == null) {
            logger.error(
                """
                    |Skipping generator task as missing required arguments:
                    |- optimizelyProjectId
                    |- optimizelyToken
                    |- packageName""".trimMargin()
            )
            return
        }
        val authenticatedRetrofit = authenticatedRetrofit(optimizelyToken)
        val experimentsService: ExperimentsService = buildExperimentsService(authenticatedRetrofit)
        val featuresService: FeaturesService = buildFeaturesService(authenticatedRetrofit)
        val experimentsGenerator = ExperimentsGenerator(packageName)
        val featuresGenerator = FeaturesGenerator(packageName)
        runBlocking {
            val experiments = experimentsService.listExperiments(optimizelyProjectId)
            val experimentsCode = experimentsGenerator.generate(experiments)
            val features = featuresService.listFeatures(optimizelyProjectId)
            val featuresCode = featuresGenerator.generate(features)
            targetDir.toPackageFolder(packageName).also { packageDir ->
                packageDir.mkdirs()
                packageDir.resolve("Experiments.kt").writeText(experimentsCode)
                packageDir.resolve("Features.kt").writeText(featuresCode)
            }
        }
    }
}

private fun File.toPackageFolder(packageName: String): File =
    packageName.split(".").fold(this) { acc, s -> acc.resolve(s) }
