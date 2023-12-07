package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import android.Manifest
import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.w2sv.androidutils.generic.goToAppSettings
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.components.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.components.SnackbarAction
import com.w2sv.wifiwidget.ui.components.SnackbarKind
import com.w2sv.wifiwidget.ui.components.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.utils.isLaunchingSuppressed
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LocationAccessPermissionRequestLauncher(
    lapState: LocationAccessPermissionState,
    widgetVM: WidgetViewModel = viewModel()
) {
    lapState.requestLaunchingAction.collectAsStateWithLifecycle().value?.let { trigger ->
        when (trigger) {
            is LocationAccessPermissionRequiringAction.PinWidgetButtonPress -> LocationAccessPermissionRequest(
                lapState = lapState,
                onGranted = {
                    WidgetWifiProperty.NonIP.LocationAccessRequiring.entries.forEach {
                        widgetVM.configuration.wifiProperties[it] = true
                    }
                    widgetVM.configuration.wifiProperties.sync()
                    widgetVM.attemptWidgetPin()
                },
                onDenied = {
                    widgetVM.attemptWidgetPin()
                },
            )

            is LocationAccessPermissionRequiringAction.PropertyCheckChange -> LocationAccessPermissionRequest(
                lapState = lapState,
                onGranted = {
                    widgetVM.configuration.wifiProperties[trigger.property] = true
                },
                onDenied = {},
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationAccessPermissionRequest(
    lapState: LocationAccessPermissionState,
    onGranted: suspend (Context) -> Unit,
    onDenied: suspend (Context) -> Unit,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ),
        onPermissionsResult = { permissionToGranted ->
            scope.launch {
                when (permissionToGranted.values.all { it }) {
                    true -> {
                        onGranted(context)
                        lapState.onGranted()
                    }

                    false -> onDenied(context)
                }
                lapState.onRequestLaunched()
                lapState.setRequestLaunchingAction(null)
            }
        },
    )

    LaunchedEffect(permissionState) {
        when {
            permissionState.allPermissionsGranted -> {
                onGranted(context)
                lapState.setRequestLaunchingAction(null)
            }

            permissionState.isLaunchingSuppressed(launchedBefore = lapState.requestLaunched.value) -> {
                snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                    AppSnackbarVisuals(
                        msg = context.getString(R.string.you_need_to_go_to_the_app_settings_and_grant_location_access_permission),
                        kind = SnackbarKind.Error,
                        action = SnackbarAction(
                            label = context.getString(R.string.go_to_settings),
                            callback = {
                                goToAppSettings(context)
                            },
                        ),
                    ),
                )
                lapState.setRequestLaunchingAction(null)
            }

            else -> {
                permissionState.launchMultiplePermissionRequest()
            }
        }
    }
}
