package com.patxi.poetimizely

import java.io.File
import java.util.Properties

fun poetimizelyVersion(): String = Properties().apply {
    load(getVersioningFile().inputStream())
}.getProperty("version")

private fun getVersioningFile(): File = File("../version.properties")
