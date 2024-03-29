package io.github.patxibocos.poetimizely.gradle.plugin

import io.github.patxibocos.poetimizely.core.codeForExperiments
import io.github.patxibocos.poetimizely.core.codeForFeatures
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GeneratorTask : DefaultTask() {
    @Input
    @Optional
    var optimizelyProjectId: Long? = null

    @Input
    @Optional
    var optimizelyToken: String? = null

    @Input
    @Optional
    var packageName: String? = null

    @TaskAction
    fun doAction() {
        val mainSourceSet = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets.findByName("main")
        val targetDir = if (mainSourceSet != null) {
            val mainSrcDirs = mainSourceSet.allSource.srcDirs
            mainSrcDirs.find { it.name == "kotlin" } ?: mainSrcDirs.find { it.name == "java" }
        } else {
            project.projectDir.resolve("src/main/kotlin").takeIf { it.exists() }
                ?: project.projectDir.resolve("src/main/java").takeIf { it.exists() }
        } ?: error("Cannot find the source directory")
        val optimizelyProjectId = optimizelyProjectId
        val optimizelyToken = optimizelyToken
        val packageName = packageName
        if (optimizelyProjectId == null || optimizelyToken == null || packageName == null) {
            logger.error(
                """
                    |Skipping generator task as missing required arguments:
                    |- optimizelyProjectId
                    |- optimizelyToken
                    |- packageName"""
                    .trimMargin(),
            )
            return
        }
        runBlocking {
            val experimentsCode = codeForExperiments(optimizelyProjectId, optimizelyToken, packageName)
            val featuresCode = codeForFeatures(optimizelyProjectId, optimizelyToken, packageName)
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
