package com.w2sv.wifiwidget.screens.home

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.w2sv.wifiwidget.ui.AppTheme
import com.w2sv.wifiwidget.widget.WiFiWidgetProvider

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                HomeScreen(::requestPinWidget) {
                    locationPermissionRequestLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
            }
        }
    }

    private fun requestPinWidget() {
        with(getSystemService(AppWidgetManager::class.java)) {
            if (isRequestPinAppWidgetSupported) {
                requestPinAppWidget(
                    ComponentName(
                        this@HomeActivity,
                        WiFiWidgetProvider::class.java
                    ),
                    null,
                    null
                )
            }
        }
    }

    private val locationPermissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            requestPinWidget()
        }
}

