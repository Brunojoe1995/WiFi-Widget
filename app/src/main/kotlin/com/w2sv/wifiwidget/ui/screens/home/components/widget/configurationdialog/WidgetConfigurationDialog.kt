package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.wifiwidget.ui.designsystem.ConfigurationDialog
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.ColorPickerDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.ColorPickerProperties
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.PropertyInfoDialog
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.InfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.utils.isLandscapeModeActivated
import com.w2sv.wifiwidget.ui.utils.thenIf

@Composable
fun WidgetConfigurationDialog(
    locationAccessState: LocationAccessState,
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var infoDialogData by rememberSaveable(
        stateSaver = InfoDialogData.nullableStateSaver,
    ) {
        mutableStateOf(null)
    }
    var colorPickerProperties by rememberSaveable(
        stateSaver = ColorPickerProperties.nullableStateSaver,
    ) {
        mutableStateOf(null)
    }

    ConfigurationDialog(
        onDismissRequest = remember {
            {
                widgetConfiguration.reset()
                closeDialog()
            }
        },
        onApplyButtonPress = remember {
            {
                widgetConfiguration.launchSync()
                closeDialog()
            }
        },
        modifier = modifier.thenIf(
            condition = isLandscapeModeActivated,
            onTrue = { fillMaxHeight() }
        ),
        iconRes = com.w2sv.widget.R.drawable.ic_settings_24,
        title = stringResource(id = com.w2sv.common.R.string.configure_widget),
        applyButtonEnabled = widgetConfiguration.statesDissimilar.collectAsStateWithLifecycle().value,
    ) {
        WidgetConfigurationDialogContent(
            widgetConfiguration = widgetConfiguration,
            locationAccessState = locationAccessState,
            showPropertyInfoDialog = remember { { infoDialogData = it } },
            showCustomColorConfigurationDialog = remember { { colorPickerProperties = it } },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        )

        // Show PropertyInfoDialog if applicable
        infoDialogData?.let {
            PropertyInfoDialog(
                data = it,
                onDismissRequest = { infoDialogData = null }
            )
        }
        colorPickerProperties?.let { properties ->
            ColorPickerDialog(
                properties = properties,
                applyColor = remember {
                    {
                        widgetConfiguration.customColoringData.value =
                            properties.createCustomColoringData(widgetConfiguration.customColoringData.value)
                    }
                },
                onDismissRequest = {
                    colorPickerProperties = null
                },
            )
        }
    }
}
