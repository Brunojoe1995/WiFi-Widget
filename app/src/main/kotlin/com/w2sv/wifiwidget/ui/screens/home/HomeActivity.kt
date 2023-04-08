@file:Suppress("DEPRECATION")

package com.w2sv.wifiwidget.ui.screens.home

import android.animation.ObjectAnimator
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.w2sv.androidutils.SelfManagingLocalBroadcastReceiver
import com.w2sv.androidutils.extensions.getIntExtraOrNull
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.androidutils.extensions.locationServicesEnabled
import com.w2sv.androidutils.extensions.reset
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.common.Theme
import com.w2sv.common.WidgetColorSection
import com.w2sv.common.WifiProperty
import com.w2sv.common.extensions.addObservers
import com.w2sv.common.preferences.CustomWidgetColors
import com.w2sv.common.preferences.EnumOrdinals
import com.w2sv.common.preferences.FloatPreferences
import com.w2sv.common.preferences.GlobalFlags
import com.w2sv.common.preferences.WidgetRefreshingParameters
import com.w2sv.common.preferences.WifiProperties
import com.w2sv.kotlinutils.extensions.getByOrdinal
import com.w2sv.widget.WidgetDataRefreshWorker
import com.w2sv.widget.WidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.CoherentNonAppliedStates
import com.w2sv.wifiwidget.ui.NonAppliedSnapshotStateMap
import com.w2sv.wifiwidget.ui.NonAppliedStateFlow
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        private val wifiProperties: WifiProperties,
        private val globalFlags: GlobalFlags,
        private val enumOrdinals: EnumOrdinals,
        private val floatPreferences: FloatPreferences,
        private val widgetRefreshingParameters: WidgetRefreshingParameters,
        private val customWidgetColors: CustomWidgetColors,
        savedStateHandle: SavedStateHandle,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val lifecycleObservers: List<LifecycleObserver>
            get() = listOf(
                wifiProperties,
                globalFlags,
                enumOrdinals,
                floatPreferences,
                widgetRefreshingParameters,
                customWidgetColors
            )

        /**
         * onSplashScreenAnimationFinished
         */

        fun onSplashScreenAnimationFinished() {
            if (openConfigurationDialogOnSplashScreenAnimationFinished) {
                showWidgetConfigurationDialog.value = true
            }
        }

        private val openConfigurationDialogOnSplashScreenAnimationFinished =
            savedStateHandle.contains(WidgetProvider.EXTRA_OPEN_CONFIGURATION_DIALOG_ON_START)

        /**
         * In-App Theme
         */

        val inAppThemeState = NonAppliedStateFlow(
            viewModelScope,
            { getByOrdinal<Theme>(enumOrdinals.inAppTheme) },
            {
                enumOrdinals.inAppTheme = it.ordinal
                appliedInAppTheme.value = it
            }
        )

        var appliedInAppTheme = MutableStateFlow<Theme>(getByOrdinal(enumOrdinals.inAppTheme))

        /**
         * Widget Pin Listening
         */

        fun onWidgetOptionsUpdated(widgetId: Int, context: Context) {
            if (widgetIds.add(widgetId)) {
                onNewWidgetPinned(widgetId, context)
            }
        }

        private fun onNewWidgetPinned(widgetId: Int, context: Context) {
            i { "Pinned new widget w ID=$widgetId" }
            context.showToast(R.string.pinned_widget)

            if (wifiProperties.getValue(WifiProperty.SSID.name) && !context.locationServicesEnabled)
                context.showToast(
                    R.string.ssid_display_requires_location_services_to_be_enabled,
                    Toast.LENGTH_LONG
                )
        }

        private val widgetIds: MutableSet<Int> =
            WidgetProvider.getWidgetIds(context).toMutableSet()

        /**
         * Widget Configuration
         */

        val showWidgetConfigurationDialog = MutableStateFlow(false)

        val propertyInfoDialogIndex: MutableStateFlow<Int?> = MutableStateFlow(null)

        val widgetPropertyStateMap = NonAppliedSnapshotStateMap(
            { wifiProperties },
            { wifiProperties.putAll(it) }
        )

        val widgetThemeState = NonAppliedStateFlow(
            viewModelScope,
            { getByOrdinal<Theme>(enumOrdinals.widgetTheme) },
            { enumOrdinals.widgetTheme = it.ordinal }
        )

        val customThemeSelected = widgetThemeState.transform {
            emit(it == Theme.Custom)
        }

        val customWidgetColorsState = NonAppliedSnapshotStateMap(
            { customWidgetColors },
            { customWidgetColors.putAll(it) }
        )

        val customizationDialogSection = MutableStateFlow<WidgetColorSection?>(null)

        fun onDismissCustomizationDialog() {
            customizationDialogSection.reset()
        }

        val widgetOpacityState = NonAppliedStateFlow(
            viewModelScope,
            { floatPreferences.opacity },
            { floatPreferences.opacity = it }
        )

        val widgetRefreshingParametersState = NonAppliedSnapshotStateMap(
            { widgetRefreshingParameters },
            {
                widgetRefreshingParameters.putAll(it)
                widgetRefreshingParametersChanged.value = true
            }
        )

        val widgetRefreshingParametersChanged = MutableStateFlow(false)

        val widgetConfigurationStates = CoherentNonAppliedStates(
            widgetPropertyStateMap,
            widgetThemeState,
            widgetOpacityState,
            widgetRefreshingParametersState,
            customWidgetColorsState,
            coroutineScope = viewModelScope
        )

        fun onDismissWidgetConfigurationDialog() {
            widgetConfigurationStates.reset()
            showWidgetConfigurationDialog.value = false
        }

        /**
         * @return Boolean indicating whether change has been confirmed
         */
        fun confirmAndSyncPropertyChange(
            property: WifiProperty,
            value: Boolean,
            onChangeRejected: () -> Unit
        ) {
            when (value || widgetPropertyStateMap.values.count { true } != 1) {
                true -> widgetPropertyStateMap[property.name] = value
                false -> onChangeRejected()
            }
        }

        /**
         * lap := Location Access Permission
         */

        var lapDialogAnswered: Boolean by globalFlags::locationPermissionDialogAnswered

        val lapDialogTrigger: MutableStateFlow<LocationAccessPermissionDialogTrigger?> =
            MutableStateFlow(null)

        /**
         * BackPress
         */

        var exitOnBackPress: Boolean = false
            private set

        fun onFirstBackPress(context: Context) {
            exitOnBackPress = true
            context.showToast("Tap again to exit")
            viewModelScope.launchDelayed(2500L) {
                exitOnBackPress = false
            }
        }
    }

    private val viewModel by viewModels<ViewModel>()

    class AppWidgetOptionsChangedReceiver(
        broadcastManager: LocalBroadcastManager,
        callback: (Context?, Intent?) -> Unit
    ) : SelfManagingLocalBroadcastReceiver.Impl(
        broadcastManager,
        IntentFilter(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED),
        callback
    )

    val lapRequestLauncher by lazy {
        LocationAccessPermissionHandler(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        handleSplashScreen {
            viewModel.onSplashScreenAnimationFinished()
        }

        super.onCreate(savedInstanceState)

        addObservers(
            viewModel.lifecycleObservers + listOf(
                lapRequestLauncher,
                AppWidgetOptionsChangedReceiver(LocalBroadcastManager.getInstance(this)) { _, intent ->
                    i { "WifiWidgetOptionsChangedReceiver.onReceive | ${intent?.extras?.keySet()}" }

                    intent?.getIntExtraOrNull(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                        ?.let { widgetId ->
                            viewModel.onWidgetOptionsUpdated(
                                widgetId,
                                this
                            )
                        }
                }
            )
        )

        subscribeToFlows()

        setContent {
            WifiWidgetTheme(
                darkTheme = when (viewModel.appliedInAppTheme.collectAsState().value) {
                    Theme.Light -> false
                    Theme.Dark -> true
                    Theme.DeviceDefault -> isSystemInDarkTheme()
                    else -> throw Error()
                }
            ) {
                HomeScreen {
                    finishAffinity()
                }
            }
        }
    }

    /**
     * Sets SwipeUp exit animation and triggers call to [onAnimationFinished] in time.
     */
    private fun handleSplashScreen(onAnimationFinished: () -> Unit) {
        installSplashScreen().setOnExitAnimationListener { splashScreenViewProvider ->
            ObjectAnimator.ofFloat(
                splashScreenViewProvider.view,
                View.TRANSLATION_Y,
                0f,
                -splashScreenViewProvider.view.height.toFloat()
            )
                .apply {
                    interpolator = AnticipateInterpolator()
                    duration = 400L
                    doOnEnd {
                        splashScreenViewProvider.remove()
                        onAnimationFinished()
                    }
                }
                .start()
        }
    }

    private fun subscribeToFlows() {
        lifecycleScope.launch {
            with(viewModel.widgetRefreshingParametersChanged) {
                collect {
                    if (it) {
                        WidgetDataRefreshWorker
                            .Administrator
                            .getInstance(applicationContext)
                            .applyChangedParameters()
                        value = false
                    }
                }
            }
        }
    }
}