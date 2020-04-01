package com.patxi.poetimizely

import java.io.File
import java.util.Properties

fun poetimizelyVersion(versionPropertiesFile: File): String = Properties().run {
    load(versionPropertiesFile.inputStream())
    val major = get("version.major")
    val minor = get("version.minor")
    val patch = get("version.patch")
    "$major.$minor.$patch"
}
