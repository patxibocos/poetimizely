package com.patxi.poetimizely

import java.io.File
import java.util.Properties

fun poetimizelyVersion(): String = Properties().apply {
    load(getVersioningFile().inputStream())
}.getProperty("version")

fun bumpVersionTo(version: String): Unit = Properties().apply {
    put("version", version)
}.store(getVersioningFile().outputStream(), null)

private fun getVersioningFile(): File = File("version.properties")