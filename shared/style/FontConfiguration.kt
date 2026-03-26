package com.example.miram.shared.style

import android.content.Context
import android.content.res.Configuration

fun Context.withFixedFontScale(): Context {
    val overrideConfiguration = Configuration(resources.configuration).apply {
        fontScale = 1.0f
    }
    return createConfigurationContext(overrideConfiguration)
}

fun Configuration.withFixedFontScale(): Configuration =
    Configuration(this).apply {
        fontScale = 1.0f
    }
