package com.patxi.poetimizely.maven.plugin

import com.patxi.poetimizely.generator.codeForExperiments
import com.patxi.poetimizely.generator.codeForFeatures
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.apache.maven.plugin.testing.MojoRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

class GeneratorMojoTest {

    @get:Rule
    val rule = MojoRule()

    @Test
    fun `A Maven project with the plugin applied contains the poetimize goal and is able to execute it`() {
        val testPom = File("src/test/resources/test-pom.xml")
        val generatorMojo = rule.lookupMojo("poetimize", testPom) as GeneratorMojo?
        requireNotNull(generatorMojo)
        val sourceDirectory = "src/test/kotlin"
        generatorMojo.project = mockk {
            every { compileSourceRoots } returns listOf(sourceDirectory)
        }
        val experimentsCode = "experiments generated code we don't care at this point"
        val featuresCode = "features generated code we don't care at this point"
        mockCodeGenerators(generatorMojo, experimentsCode, featuresCode)

        generatorMojo.execute()

        assertEquals(123_456_789L, generatorMojo.optimizelyProjectId)
        assertEquals("t0k€n", generatorMojo.optimizelyToken)
        assertEquals("what.ever.pack.age", generatorMojo.packageName)
        with(File("$sourceDirectory/what/ever/pack/age/Experiments.kt")) {
            assertTrue(exists())
            assertEquals(experimentsCode, readText())
            delete()
        }
        with(File("$sourceDirectory/what/ever/pack/age/Features.kt")) {
            assertTrue(exists())
            assertEquals(featuresCode, readText())
            delete()
        }
    }

    private fun mockCodeGenerators(generatorMojo: GeneratorMojo, experimentsCode: String, featuresCode: String) {
        mockkStatic("com.patxi.poetimizely.generator.Api")
        coEvery {
            codeForExperiments(generatorMojo.optimizelyProjectId, generatorMojo.optimizelyToken, generatorMojo.packageName)
        } returns experimentsCode
        coEvery {
            codeForFeatures(generatorMojo.optimizelyProjectId, generatorMojo.optimizelyToken, generatorMojo.packageName)
        } returns featuresCode
    }
}
