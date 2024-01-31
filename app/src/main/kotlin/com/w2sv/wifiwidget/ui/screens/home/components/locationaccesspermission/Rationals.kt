package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.DialogButton
import com.w2sv.wifiwidget.ui.components.InfoIcon
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.utils.CollectFromFlow
import com.w2sv.wifiwidget.ui.utils.styledTextResource

@Immutable
sealed interface LocationAccessPermissionRequestTrigger {
    @Immutable
    data object InitialAppLaunch : LocationAccessPermissionRequestTrigger

    @Immutable
    data class PropertyCheckChange(val property: WidgetWifiProperty.NonIP.LocationAccessRequiring) :
        LocationAccessPermissionRequestTrigger
}

@Immutable
sealed interface LocationAccessPermissionStatus {
    @Immutable
    data object NotGranted : LocationAccessPermissionStatus

    @Immutable
    data class Granted(val trigger: LocationAccessPermissionRequestTrigger?) :
        LocationAccessPermissionStatus

    companion object {
        fun get(isGranted: Boolean): LocationAccessPermissionStatus =
            if (isGranted) Granted(null) else NotGranted
    }
}

@Composable
fun LocationAccessRationals(state: LocationAccessState) {
    if (state.showRational.collectAsStateWithLifecycle().value) {
        LocationAccessPermissionRational(
            onProceed = {
                state.onRationalShown()
            },
        )
    }
    state.backgroundAccessState?.let { backgroundAccessState ->
        var showRational by rememberSaveable {
            mutableStateOf(false)
        }

        CollectFromFlow(backgroundAccessState.showRational) {
            showRational = true
        }

        if (showRational) {
            BackgroundLocationAccessRational(
                launchPermissionRequest = { backgroundAccessState.launchRequest() },
                onDismissRequest = {
                    showRational = false
                }
            )
        }
    }
}

@Composable
private fun LocationAccessPermissionRational(
    onProceed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        icon = {
            InfoIcon()
        },
        text = {
            Text(
                text = styledTextResource(id = R.string.location_access_permission_rational),
                textAlign = TextAlign.Center,
            )
        },
        confirmButton = {
            DialogButton(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(text = stringResource(R.string.understood)) }
        },
        onDismissRequest = onProceed,
    )
}

@Composable
private fun BackgroundLocationAccessRational(
    launchPermissionRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(
                onClick = {
                    launchPermissionRequest()
                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(id = R.string.grant))
            }
        },
        dismissButton = {
            DialogButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.maybe_later))
            }
        },
        icon = {
            InfoIcon()
        },
        text = {
            Text(text = styledTextResource(id = R.string.background_location_access_rational))
        },
    )
}