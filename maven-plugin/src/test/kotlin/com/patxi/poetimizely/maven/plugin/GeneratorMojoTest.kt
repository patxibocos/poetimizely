package com.patxi.poetimizely.maven.plugin

import org.apache.maven.plugin.testing.MojoRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import java.io.File

class GeneratorMojoTest {

    @Rule
    @JvmField
    val rule = MojoRule()

    @Test
    fun `A Maven project with the plugin applied contains the poetimize goal`() {
        val testPom = File("src/test/resources/test-pom.xml")

        val generatorMojo = rule.lookupMojo("poetimize", testPom) as GeneratorMojo?

        assertNotNull(generatorMojo)
        assertEquals(123_456_789L, generatorMojo?.optimizelyProjectId)
        assertEquals("t0k€n", generatorMojo?.optimizelyToken)
        assertEquals("what.ever.pack.age", generatorMojo?.packageName)
    }
}
