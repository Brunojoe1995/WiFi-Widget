package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.common.R
import com.w2sv.wifiwidget.ui.components.CustomDialog
import com.w2sv.wifiwidget.ui.components.DialogButtonRow
import com.w2sv.wifiwidget.ui.components.DialogHeaderProperties
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.utils.conditional
import com.w2sv.wifiwidget.ui.utils.isLandscapeModeActivated
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun WidgetConfigurationDialog(
    locationAccessState: LocationAccessState,
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
    widgetVM: WidgetViewModel = viewModel(),
    scope: CoroutineScope = rememberCoroutineScope(),
) {
    val onDismissRequest: () -> Unit = {
        scope.launch {
            widgetVM.configuration.reset()
        }
        closeDialog()
    }

    CustomDialog(
        headerProperties = DialogHeaderProperties(
            title = stringResource(id = R.string.configure_widget),
            icon = {
                Icon(
                    painterResource(id = com.w2sv.widget.R.drawable.ic_settings_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
        ),
        onDismissRequest = onDismissRequest,
        modifier = modifier.conditional(isLandscapeModeActivated, { fillMaxHeight() }),
    ) {
        WidgetConfigurationDialogContent(
            widgetConfiguration = widgetVM.configuration,
            locationAccessState = locationAccessState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
                .fillMaxHeight(0.75f),
        )
        DialogButtonRow(
            onCancel = {
                onDismissRequest()
            },
            onApply = {
                widgetVM.configuration.launchSync()
                closeDialog()
            },
            applyButtonEnabled = widgetVM.configuration.statesDissimilar.collectAsStateWithLifecycle().value,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
