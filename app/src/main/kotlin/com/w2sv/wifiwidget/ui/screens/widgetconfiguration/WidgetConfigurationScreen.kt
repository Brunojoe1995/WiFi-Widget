package com.w2sv.wifiwidget.ui.screens.widgetconfiguration

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbar
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.LocalSnackbarHostState
import com.w2sv.wifiwidget.ui.designsystem.Padding
import com.w2sv.wifiwidget.ui.designsystem.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.rememberLocationAccessPermissionState
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.ColorPickerDialog
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.ColorPickerProperties
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.PropertyInfoDialog
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.RefreshIntervalConfigurationDialog
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.InfoDialogData
import com.w2sv.wifiwidget.ui.utils.CollectLatestFromFlow
import com.w2sv.wifiwidget.ui.viewmodels.AppViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel
import kotlinx.coroutines.flow.update

@Destination<RootGraph>(start = true)
@Composable
fun WidgetConfigurationScreen(
//    locationAccessState: LocationAccessState,
    navigator: DestinationsNavigator,
    appVM: AppViewModel = hiltViewModel(),
    widgetVM: WidgetViewModel = hiltViewModel(),
    context: Context = LocalContext.current
) {
    Scaffold(
        snackbarHost = {
            val snackbarHostState = LocalSnackbarHostState.current

            // Show Snackbars collected from sharedSnackbarVisuals
            CollectLatestFromFlow(appVM.snackbarVisualsFlow) {
                snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(it(context))
            }

            SnackbarHost(snackbarHostState) { snackbarData ->
                AppSnackbar(visuals = snackbarData.visuals as AppSnackbarVisuals)
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding() + 16.dp)
        ) {
            BackButtonHeaderWithDivider(
                title = stringResource(id = R.string.widget_configuration),
                onBackButtonClick = remember { { navigator.popBackStack() } }
            )

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
            var showRefreshIntervalConfigurationDialog by rememberSaveable {
                mutableStateOf(false)
            }

            // Show PropertyInfoDialog if applicable
            infoDialogData?.let {
                PropertyInfoDialog(
                    data = it,
                    onDismissRequest = remember {
                        { infoDialogData = null }
                    }
                )
            }
            // Show ColorPickerDialog if applicable
            colorPickerProperties?.let { properties ->
                ColorPickerDialog(
                    properties = properties,
                    applyColor = remember {
                        {
                            widgetVM.configuration.coloringConfig.update {
                                it.copy(
                                    custom = properties.createCustomColoringData(
                                        it.custom
                                    )
                                )
                            }

                        }
                    },
                    onDismissRequest = remember {
                        {
                            colorPickerProperties = null
                        }
                    },
                )
            }
            if (showRefreshIntervalConfigurationDialog) {
                RefreshIntervalConfigurationDialog(
                    interval = widgetVM.configuration.refreshInterval.collectAsState().value,
                    setInterval = remember {
                        { widgetVM.configuration.refreshInterval.value = it }
                    },
                    onDismissRequest = remember {
                        { showRefreshIntervalConfigurationDialog = false }
                    }
                )
            }

            WidgetPropertyConfigurationColumn(
                widgetConfiguration = widgetVM.configuration,
                locationAccessState = rememberLocationAccessPermissionState(),
                showPropertyInfoDialog = remember { { infoDialogData = it } },
                showCustomColorConfigurationDialog = remember { { colorPickerProperties = it } },
                showRefreshIntervalConfigurationDialog = remember {
                    {
                        showRefreshIntervalConfigurationDialog = true
                    }
                }
            )
        }
    }
}

@Composable
fun BackButtonHeaderWithDivider(
    title: String,
    onBackButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Padding.horizontalDefault),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(onClick = onBackButtonClick, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 14.dp))
    }
}