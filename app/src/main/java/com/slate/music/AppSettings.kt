package com.slate.music

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppSettings {

    private const val PREFS_NAME = "app_settings"
    private const val BLUR = "glass_blur"
    private const val HAPTICS = "haptics"

    private val _isBlurEnabled = MutableStateFlow(true)
    val isBlurEnabled: StateFlow<Boolean> = _isBlurEnabled.asStateFlow()

    private val _isHapticsEnabled = MutableStateFlow(true)
    val isHapticsEnabled: StateFlow<Boolean> = _isHapticsEnabled.asStateFlow()

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isBlurEnabled.value = prefs.getBoolean(BLUR, true)
        _isHapticsEnabled.value = prefs.getBoolean(HAPTICS, true)
    }

    fun setBlurEnabled(context: Context, enabled: Boolean) {
        _isBlurEnabled.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(BLUR, enabled).apply()
    }

    fun setHapticsEnabled(context: Context, enabled: Boolean) {
        _isHapticsEnabled.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(HAPTICS, enabled).apply()
    }
}