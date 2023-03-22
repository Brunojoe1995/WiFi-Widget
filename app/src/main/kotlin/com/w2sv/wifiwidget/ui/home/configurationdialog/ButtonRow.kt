package com.w2sv.wifiwidget.ui.home.configurationdialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.extensions.resetBoolean
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.activities.HomeActivity
import com.w2sv.wifiwidget.ui.shared.DialogButton
import com.w2sv.wifiwidget.ui.shared.JostText

@Composable
internal fun StatefulButtonRow(modifier: Modifier = Modifier, viewModel: HomeActivity.ViewModel = viewModel()) {
    val context = LocalContext.current
    val applyButtonEnabled by viewModel.widgetConfigurationStates.requiringUpdate.collectAsState()

    ButtonRow(
        onCancel = {
            viewModel.onDismissWidgetConfigurationDialog()
        },
        onApply = {
            viewModel.widgetConfigurationStates.apply()
            WifiWidgetProvider.triggerDataRefresh(context)
            context.showToast(R.string.updated_widget_configuration)
            viewModel.showWidgetConfigurationDialog.resetBoolean()
        },
        applyButtonEnabled = {
            applyButtonEnabled
        },
        modifier = modifier
    )
}

@Composable
private fun ButtonRow(
    onCancel: () -> Unit,
    onApply: () -> Unit,
    applyButtonEnabled: () -> Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DialogButton(onClick = onCancel) {
            JostText(text = stringResource(R.string.cancel))
        }
        DialogButton(onClick = onApply, enabled = applyButtonEnabled()) {
            JostText(text = stringResource(R.string.apply))
        }
    }
}