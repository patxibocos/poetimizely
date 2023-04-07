package io.github.patxibocos.poetimizely.maven.plugin

import io.github.patxibocos.poetimizely.core.codeForExperiments
import io.github.patxibocos.poetimizely.core.codeForFeatures
import kotlinx.coroutines.runBlocking
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File

@Mojo(name = "poetimize")
class GeneratorMojo : AbstractMojo() {
    @Parameter(defaultValue = "\${project}", required = true, readonly = false)
    lateinit var project: MavenProject

    @Parameter(property = "poetimizely.optimizelyProjectId", required = true, readonly = false)
    var optimizelyProjectId: Long = 0L

    @Parameter(property = "poetimizely.optimizelyToken", required = true, readonly = false)
    lateinit var optimizelyToken: String

    @Parameter(property = "poetimizely.packageName", required = true, readonly = false)
    lateinit var packageName: String

    override fun execute() {
        val sourceSets = project.compileSourceRoots.map(Any?::toString)
        require(sourceSets.isNotEmpty())
        val targetDir = File(sourceSets.find { it.contains("kotlin") } ?: sourceSets.first())
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
