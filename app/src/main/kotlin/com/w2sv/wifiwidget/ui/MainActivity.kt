package com.w2sv.wifiwidget.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.common.constants.Extra
import com.w2sv.domain.model.Theme
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.wifiwidget.ui.screens.home.HomeScreen
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.viewmodels.AppViewModel
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeScreenVM by viewModels<HomeScreenViewModel>()
    private val widgetVM by viewModels<WidgetViewModel>()
    private val appVM by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        handleSplashScreen(
            onAnimationFinished = {
                if (intent.hasExtra(Extra.OPEN_WIDGET_CONFIGURATION_DIALOG)) {
                    homeScreenVM.setShowWidgetConfigurationDialog(true)
                }
            },
        )

        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            appVM.exitApplication.collect {
                finishAffinity()
            }
        }
        lifecycleScope.collectFromFlow(widgetVM.attemptWidgetPin) {
            attemptWifiWidgetPin(this)
        }

        setContent {
            AppTheme(
                useDynamicTheme = appVM.useDynamicColors.collectAsStateWithLifecycle().value,
                darkTheme = when (appVM.theme.collectAsStateWithLifecycle().value) {
                    Theme.Light -> false
                    Theme.Dark -> true
                    Theme.SystemDefault -> isSystemInDarkTheme()
                    else -> throw Error()
                },
            ) {
                HomeScreen()
            }
        }
    }

    /**
     * Sets SwipeUp exit animation and calls [onAnimationFinished].
     */
    private fun handleSplashScreen(onAnimationFinished: () -> Unit) {
        installSplashScreen().setOnExitAnimationListener { splashScreenViewProvider ->
            ObjectAnimator.ofFloat(
                splashScreenViewProvider.view,
                View.TRANSLATION_Y,
                0f,
                -splashScreenViewProvider.view.height.toFloat(),
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

    override fun onStart() {
        super.onStart()

        homeScreenVM.onStart(this)
        widgetVM.refreshWidgetIds()
    }
}
