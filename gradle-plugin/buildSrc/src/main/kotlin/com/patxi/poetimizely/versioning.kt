package com.patxi.poetimizely

import org.gradle.api.Project
import java.io.File
import java.util.Properties

fun poetimizelyVersion(project: Project): String = Properties().apply {
    load(getVersioningFile(project.projectDir).inputStream())
}.getProperty("version")

private fun getVersioningFile(path: File): File = path.parentFile.resolve("poetimizely.properties")
