package com.w2sv.wifiwidget.ui

import android.location.LocationManager
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalLocationManager =
    staticCompositionLocalOf<LocationManager> { throw UninitializedPropertyAccessException("LocalLocationManager not yet provided") }

val LocalNavHostController =
    staticCompositionLocalOf<NavHostController> {
        throw UninitializedPropertyAccessException(
            "LocalRootNavHostController not yet provided"
        )
    }

val LocalUseDarkTheme =
    compositionLocalOf<Boolean> {
        throw UninitializedPropertyAccessException(
            "LocalUseDarkTheme not yet provided"
        )
    }

val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }
