package com.w2sv.wifiwidget.ui.screens.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.extensions.reset
import com.w2sv.common.WifiProperty
import com.w2sv.widget.WidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.WidgetConfigurationViewModel
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme

@Preview
@Composable
private fun Prev() {
    WifiWidgetTheme {
        LocationAccessPermissionRationalDialog(Modifier, "Proceed without SSID", {}, {})
    }
}

enum class LocationAccessPermissionDialogTrigger {
    PinWidgetButtonPress,
    SSIDCheck
}

@Composable
fun LocationAccessPermissionRationalDialog(
    trigger: LocationAccessPermissionDialogTrigger,
    homeScreenViewModel: HomeScreenViewModel = viewModel(),
    widgetConfigurationViewModel: WidgetConfigurationViewModel = viewModel()
) {
    val context = LocalContext.current

    when (trigger) {
        LocationAccessPermissionDialogTrigger.PinWidgetButtonPress -> {
            LocationAccessPermissionRationalDialog(
                dismissButtonText = stringResource(R.string.proceed_without_ssid),
                onConfirmButtonPressed = {
                    homeScreenViewModel.lapRequestTrigger.value = trigger
                },
                onDismissButtonPressed = {
                    WidgetProvider.pinWidget(context)
                }
            )
        }

        LocationAccessPermissionDialogTrigger.SSIDCheck -> LocationAccessPermissionRationalDialog(
            dismissButtonText = stringResource(R.string.never_mind),
            onConfirmButtonPressed = {
                homeScreenViewModel.lapRequestTrigger.value = trigger
            },
            onDismissButtonPressed = {
                widgetConfigurationViewModel.widgetPropertyStateMap[WifiProperty.SSID] = false
            }
        )
    }
}

@Composable
private fun LocationAccessPermissionRationalDialog(
    modifier: Modifier = Modifier,
    dismissButtonText: String,
    onConfirmButtonPressed: () -> Unit,
    onDismissButtonPressed: () -> Unit,
    viewModel: HomeScreenViewModel = viewModel()
) {
    AlertDialog(
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            JostText(
                text = stringResource(R.string.lap_dialog_title),
                textAlign = TextAlign.Center
            )
        },
        text = {
            JostText(
                text = stringResource(id = R.string.lap_dialog_text),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            DialogButton(
                {
                    onConfirmButtonPressed()
                    viewModel.onLAPDialogAnswered()
                    viewModel.lapDialogTrigger.reset()
                },
                modifier = Modifier.fillMaxWidth()
            ) { JostText(text = stringResource(R.string.go_ahead)) }
        },
        dismissButton = {
            DialogButton({
                onDismissButtonPressed()
                viewModel.onLAPDialogAnswered()
                viewModel.lapDialogTrigger.reset()
            }, modifier = Modifier.fillMaxWidth()) { JostText(text = dismissButtonText) }
        },
        onDismissRequest = {
            viewModel.lapDialogTrigger.reset()
        }
    )
}