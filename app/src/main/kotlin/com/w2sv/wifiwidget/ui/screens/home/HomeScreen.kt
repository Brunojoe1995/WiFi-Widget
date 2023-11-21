package com.w2sv.wifiwidget.ui.screens.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppFontText
import com.w2sv.wifiwidget.ui.components.AppSnackbar
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.AppTopBar
import com.w2sv.wifiwidget.ui.components.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.components.drawer.NavigationDrawer
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.BackgroundLocationAccessRationalDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRationalDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequest
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequiringAction
import com.w2sv.wifiwidget.ui.screens.home.components.widget.WidgetCard
import com.w2sv.wifiwidget.ui.screens.home.components.widget.WidgetInteractionElementsRow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.WidgetConfigurationDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.PropertyInfoDialog
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.WifiConnectionInfoCard
import com.w2sv.wifiwidget.ui.utils.isLandscapeModeActivated
import com.w2sv.wifiwidget.ui.viewmodels.AppViewModel
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
internal fun HomeScreen(
    appViewModel: AppViewModel = viewModel(),
    widgetVM: WidgetViewModel = viewModel(),
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    NavigationDrawer(
        state = drawerState,
    ) {
        Scaffold(
            topBar = {
                AppTopBar {
                    scope.launch {
                        drawerState.open()
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState) { snackbarData ->
                    AppSnackbar(visuals = snackbarData.visuals as AppSnackbarVisuals)
                }
            },
        ) { paddingValues ->
            if (isLandscapeModeActivated) {
                LandscapeMode(paddingValues = paddingValues)
            } else {
                PortraitMode(paddingValues = paddingValues)
            }
        }

        OverlayDialogs()

        LaunchedEffect(snackbarHostState) {
            widgetVM.snackbarVisuals.collect {
                snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(it)
            }
        }

        BackHandler {
            when (drawerState.isOpen) {
                true -> scope.launch {
                    drawerState.close()
                }

                false -> appViewModel.onBackPress(context)
            }
        }
    }
}

@Composable
fun LandscapeMode(
    paddingValues: PaddingValues,
    homeScreenVM: HomeScreenViewModel = viewModel(),
    context: Context = LocalContext.current
) {
    Row(
        modifier = Modifier
            .padding(paddingValues)
            .padding(vertical = 16.dp)
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        WifiConnectionInfoCard(
            wifiStatus = homeScreenVM.wifiStatusUIState.status.collectAsStateWithLifecycle().value,
            wifiPropertiesViewData = homeScreenVM.wifiStatusUIState.propertiesViewData.collectAsStateWithLifecycle().value,
            modifier = Modifier.fillMaxWidth(0.4f),
        )

        WidgetCard(
            widgetInteractionElementsRow = {
                WidgetInteractionElementsRow(
                    onPinWidgetButtonClick = {
                        when (homeScreenVM.lapUIState.rationalShown) {
                            false ->
                                homeScreenVM.lapUIState.rationalTriggeringAction.value =
                                    LocationAccessPermissionRequiringAction.PinWidgetButtonPress

                            true -> attemptWifiWidgetPin(context)
                        }
                    },
                    onWidgetConfigurationButtonClick = {
                        homeScreenVM.setShowWidgetConfigurationDialog(true)
                    },
                )
            },
            modifier = Modifier.fillMaxWidth(0.6f),
        )
    }
}

@Composable
private fun PortraitMode(
    paddingValues: PaddingValues,
    homeScreenVM: HomeScreenViewModel = viewModel(),
    context: Context = LocalContext.current
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.15f))
        WifiConnectionInfoCard(
            wifiStatus = homeScreenVM.wifiStatusUIState.status.collectAsStateWithLifecycle().value,
            wifiPropertiesViewData = homeScreenVM.wifiStatusUIState.propertiesViewData.collectAsStateWithLifecycle().value,
            modifier = Modifier
                .weight(0.8f)
                .fillMaxWidth(0.77f),
        )

        Spacer(Modifier.weight(0.2f))

        WidgetCard(
            widgetInteractionElementsRow = {
                WidgetInteractionElementsRow(
                    onPinWidgetButtonClick = {
                        when (homeScreenVM.lapUIState.rationalShown) {
                            false ->
                                homeScreenVM.lapUIState.rationalTriggeringAction.value =
                                    LocationAccessPermissionRequiringAction.PinWidgetButtonPress

                            true -> attemptWifiWidgetPin(context)
                        }
                    },
                    onWidgetConfigurationButtonClick = {
                        homeScreenVM.setShowWidgetConfigurationDialog(true)
                    },
                )
            },
            modifier = Modifier.fillMaxWidth(0.8f),
        )
        Spacer(Modifier.weight(0.3f))
        CopyrightText(modifier = Modifier.padding(bottom = 10.dp))
    }
}

@Composable
private fun OverlayDialogs(
    homeScreenVM: HomeScreenViewModel = viewModel(),
    widgetVM: WidgetViewModel = viewModel(),
    context: Context = LocalContext.current
) {
    homeScreenVM.lapUIState.rationalTriggeringAction.collectAsStateWithLifecycle().value?.let {
        LocationAccessPermissionRationalDialog(
            onProceed = {
                homeScreenVM.lapUIState.onRationalShown()
            },
        )
    }
    homeScreenVM.lapUIState.requestLaunchingAction.collectAsStateWithLifecycle().value?.let { trigger ->
        when (trigger) {
            is LocationAccessPermissionRequiringAction.PinWidgetButtonPress -> LocationAccessPermissionRequest(
                lapUIState = homeScreenVM.lapUIState,
                onGranted = {
                    widgetVM.configuration.wifiProperties[WidgetWifiProperty.SSID] = true
                    widgetVM.configuration.wifiProperties[WidgetWifiProperty.BSSID] = true
                    widgetVM.configuration.wifiProperties.sync()
                    attemptWifiWidgetPin(context)
                },
                onDenied = {
                    attemptWifiWidgetPin(context)
                },
            )

            is LocationAccessPermissionRequiringAction.PropertyCheckChange -> LocationAccessPermissionRequest(
                lapUIState = homeScreenVM.lapUIState,
                onGranted = {
                    widgetVM.configuration.wifiProperties[trigger.property] = true
                },
                onDenied = {},
            )
        }
    }
    if (homeScreenVM.showWidgetConfigurationDialog.collectAsStateWithLifecycle().value) {
        WidgetConfigurationDialog(
            closeDialog = {
                homeScreenVM.setShowWidgetConfigurationDialog(false)
            },
        )

        widgetVM.propertyInfoDialogData.collectAsStateWithLifecycle().value?.let {
            PropertyInfoDialog(
                data = it,
                onDismissRequest = { widgetVM.setPropertyInfoDialogData(null) },
            )
        }
    }
    @SuppressLint("NewApi")
    if (homeScreenVM.lapUIState.showBackgroundAccessRational.collectAsStateWithLifecycle().value) {
        BackgroundLocationAccessRationalDialog(
            onDismissRequest = {
                homeScreenVM.lapUIState.showBackgroundAccessRational.value = false
            },
        )
    }
}

@Composable
private fun CopyrightText(modifier: Modifier = Modifier) {
    AppFontText(
        text = stringResource(R.string.copyright_text, Calendar.getInstance().get(Calendar.YEAR)),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 16.sp,
        modifier = modifier,
    )
}
