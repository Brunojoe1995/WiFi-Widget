package com.w2sv.wifiwidget.ui.states

import android.Manifest
import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.w2sv.androidutils.openAppSettings
import com.w2sv.common.utils.log
import com.w2sv.composed.permissions.extensions.isLaunchingSuppressed
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.SnackbarAction
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.designsystem.dismissCurrentAndShow
import com.w2sv.wifiwidget.ui.screens.home.components.EnableLocationAccessDependentProperties
import com.w2sv.wifiwidget.ui.screens.home.components.LocationAccessPermissionOnGrantAction
import com.w2sv.wifiwidget.ui.screens.home.components.LocationAccessPermissionStatus
import com.w2sv.wifiwidget.ui.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
fun rememberLocationAccessState(
    appVM: AppViewModel = hiltViewModel(),
    scope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    context: Context = LocalContext.current
): LocationAccessState =
    rememberLocationAccessState(
        requestLaunchedBefore = appVM.locationAccessPermissionRequested,
        saveRequestLaunchedBefore = appVM::saveLocationAccessPermissionRequested,
        rationalShown = appVM.locationAccessRationalShown,
        saveRationalShown = appVM::saveLocationAccessRationalShown,
        scope = scope,
        snackbarHostState = snackbarHostState,
        context = context
    )

@Composable
private fun rememberLocationAccessState(
    requestLaunchedBefore: Flow<Boolean>,
    saveRequestLaunchedBefore: () -> Unit,
    rationalShown: Flow<Boolean>,
    saveRationalShown: () -> Unit,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    context: Context
): LocationAccessState {
    // Necessary for connecting permissionState.onPermissionsResult & LocationAccessState.onRequestResult
    val requestResult = remember {
        MutableSharedFlow<Boolean>()
    }

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
        onPermissionsResult = {
            scope.launch {
                requestResult.emit(it.values.all { it })
            }
        }
    )

    val backgroundAccessState = rememberBackgroundLocationAccessState()

    return remember(scope, snackbarHostState, context) {
        LocationAccessState(
            permissionsState = permissionState,
            requestResult = requestResult,
            backgroundAccessState = backgroundAccessState,
            requestLaunchedBefore = requestLaunchedBefore.stateIn(scope, SharingStarted.Eagerly, false),
            saveRequestLaunchedBefore = saveRequestLaunchedBefore,
            rationalShown = rationalShown.stateIn(scope, SharingStarted.Eagerly, true),
            saveRationalShown = saveRationalShown,
            snackbarHostState = snackbarHostState,
            scope = scope,
            context = context
        )
    }
}

@Stable
class LocationAccessState(
    permissionsState: MultiplePermissionsState,
    requestResult: SharedFlow<Boolean>,
    val backgroundAccessState: BackgroundLocationAccessState?,
    private val requestLaunchedBefore: StateFlow<Boolean>,
    private val saveRequestLaunchedBefore: () -> Unit,
    val rationalShown: StateFlow<Boolean>,
    private val saveRationalShown: () -> Unit,
    private val snackbarHostState: SnackbarHostState,
    private val scope: CoroutineScope,
    private val context: Context
) : MultiplePermissionsState by permissionsState {

    val newStatus get() = _newStatus.asSharedFlow()
    private val _newStatus =
        MutableSharedFlow<LocationAccessPermissionStatus>()

    init {
        snapshotFlow { allPermissionsGranted }.collectOn(scope) { granted ->
            _newStatus.emit(
                when (granted) {
                    true -> LocationAccessPermissionStatus.Granted(onGrantAction.also { onGrantAction = null })
                    false -> LocationAccessPermissionStatus.NotGranted
                }
                    .log { "Emitted newStatus=$it" }
            )
        }

        requestResult.collectOn(scope) { granted ->
            if (!requestLaunchedBefore.value) {
                saveRequestLaunchedBefore()
            }
            if (granted) {
                backgroundAccessState?.showRationalIfPermissionNotGranted()
            }
        }
    }

    // ===================
    // Requesting
    // ===================

    fun launchMultiplePermissionRequest(
        onGrantAction: LocationAccessPermissionOnGrantAction?,
        skipSnackbarIfInAppPromptingSuppressed: Boolean = false
    ) {
        fun setOnGrantActionAnd(block: () -> Unit) {
            this.onGrantAction = onGrantAction
            block()
        }

        when {
            isLaunchingSuppressed(requestLaunchedBefore.value) && !skipSnackbarIfInAppPromptingSuppressed -> scope.launch {
                snackbarHostState.dismissCurrentAndShow(
                    AppSnackbarVisuals(
                        msg = context.getString(R.string.you_need_to_go_to_the_app_settings_and_grant_location_access_permission),
                        kind = SnackbarKind.Warning,
                        action = SnackbarAction(
                            label = context.getString(R.string.go_to_app_settings),
                            callback = {
                                setOnGrantActionAnd { context.openAppSettings() }
                            }
                        )
                    )
                )
            }

            isLaunchingSuppressed(requestLaunchedBefore.value) -> setOnGrantActionAnd { context.openAppSettings() }
            else -> setOnGrantActionAnd { launchMultiplePermissionRequest() }
        }
    }

    private var onGrantAction: LocationAccessPermissionOnGrantAction? = null

    // ===================
    // Rational
    // ===================

    fun onRationalShown() {
        saveRationalShown()
        launchMultiplePermissionRequest(
            onGrantAction = EnableLocationAccessDependentProperties,
            skipSnackbarIfInAppPromptingSuppressed = true
        )
    }
}
