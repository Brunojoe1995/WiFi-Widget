package com.w2sv.wifiwidget.screens.home

import android.Manifest
import android.animation.ObjectAnimator
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.SavedStateHandle
import com.w2sv.androidutils.ActivityCallContractHandler
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.wifiwidget.AppActivity
import com.w2sv.wifiwidget.preferences.GlobalFlags
import com.w2sv.wifiwidget.preferences.WidgetProperties
import com.w2sv.wifiwidget.ui.AppTheme
import com.w2sv.wifiwidget.utils.getMutableStateMap
import com.w2sv.wifiwidget.widget.PendingIntentCode
import com.w2sv.wifiwidget.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.widget.extensions.showPinnedWidgetToast
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppActivity() {

    companion object {
        const val EXTRA_OPEN_PROPERTIES_CONFIGURATION_DIALOG =
            "com.w2sv.wifiwidget.extra.OPEN_PROPERTIES_CONFIGURATION_DIALOG"
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        private val widgetProperties: WidgetProperties,
        private val globalFlags: GlobalFlags,
        savedStateHandle: SavedStateHandle
    ) : androidx.lifecycle.ViewModel() {

        val openPropertiesConfigurationDialog = MutableStateFlow(
            savedStateHandle.contains(EXTRA_OPEN_PROPERTIES_CONFIGURATION_DIALOG)
                .also { i{"openPropertiesConfigurationDialog: $it"} }
        )

        val widgetPropertyStates: SnapshotStateMap<String, Boolean> by lazy {
            widgetProperties.getMutableStateMap()
        }

        fun unchecksAllProperties(
            newValue: Boolean,
            preferenceKey: String
        ): Boolean =
            !newValue && widgetPropertyStates.all { (k, v) -> k == preferenceKey || !v }

        /**
         * @return flag indicating whether any property has been updated
         */
        fun syncWidgetProperties(): Boolean {
            var updatedAnyProperty = false

            widgetPropertyStates.forEach { (k, v) ->
                if (v != widgetProperties.getValue(k)) {
                    widgetProperties[k] = v
                    updatedAnyProperty = true
                }
            }

            return updatedAnyProperty
        }

        /**
         * lap := Location Access Permission
         */

        val lapDialogAnswered: Boolean by globalFlags::locationPermissionDialogAnswered

        fun onLapDialogAnswered() {
            globalFlags.locationPermissionDialogAnswered = true
        }
    }

    @Inject
    lateinit var globalFlags: GlobalFlags

    @Inject
    lateinit var widgetProperties: WidgetProperties

    override val lifecycleObservers: List<LifecycleObserver>
        get() = listOf(globalFlags, widgetProperties, lapRequestLauncher)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener(
            SwipeUpAnimation()
        )

        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                HomeScreen()
            }
        }
    }

    fun requestWidgetPin() {
        getSystemService(AppWidgetManager::class.java).let {
            if (it.isRequestPinAppWidgetSupported) {
                it.requestPinAppWidget(
                    ComponentName(
                        this,
                        WifiWidgetProvider::class.java
                    ),
                    null,
                    PendingIntent.getBroadcast(
                        this,
                        PendingIntentCode.BroadcastToWidgetPinnedReceiver.ordinal,
                        Intent(this, WidgetPinnedReceiver::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            } else
                showToast("Widget pinning not supported by your device launcher")
        }
    }

    class WidgetPinnedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            i { "WidgetPinnedReceiver.onReceive" }

            context?.showPinnedWidgetToast()
        }
    }

    val lapRequestLauncher by lazy {
        LocationAccessPermissionRequestLauncher(this) { permissionGrantedMap ->
            if (permissionGrantedMap.containsValue(true)) {
                with(viewModels<ViewModel>().value) {
                    widgetPropertyStates[widgetProperties::SSID.name] = true
                    syncWidgetProperties()
                }
            }

            requestWidgetPin()
        }
    }
}

class LocationAccessPermissionRequestLauncher(
    activity: ComponentActivity,
    override val resultCallback: (Map<String, Boolean>) -> Unit
) :
    ActivityCallContractHandler.Impl<Array<String>, Map<String, Boolean>>(
        activity,
        ActivityResultContracts.RequestMultiplePermissions()
    ) {

    fun launch() {
        resultLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
}

private class SwipeUpAnimation : SplashScreen.OnExitAnimationListener {
    override fun onSplashScreenExit(splashScreenViewProvider: SplashScreenViewProvider) {
        ObjectAnimator.ofFloat(
            splashScreenViewProvider.view,
            View.TRANSLATION_Y,
            0f,
            -splashScreenViewProvider.view.height.toFloat()
        )
            .apply {
                interpolator = AnticipateInterpolator()
                duration = 400L
                doOnEnd { splashScreenViewProvider.remove() }
            }
            .start()
    }
}