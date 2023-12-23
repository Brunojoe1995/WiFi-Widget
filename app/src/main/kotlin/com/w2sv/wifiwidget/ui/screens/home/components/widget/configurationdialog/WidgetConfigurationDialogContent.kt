package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.model.WidgetButton
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.IconHeader
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.OpacitySliderWithLabel
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.PropertyCheckRows
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.ThemeSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyInfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.getInfoDialogData
import com.w2sv.wifiwidget.ui.utils.ShakeConfig
import com.w2sv.wifiwidget.ui.utils.ShakeController
import com.w2sv.wifiwidget.ui.utils.shake
import com.w2sv.wifiwidget.ui.utils.toColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Composable
fun WidgetConfigurationDialogContent(
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    locationAccessState: LocationAccessState,
    showPropertyInfoDialog: (PropertyInfoDialogData) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState()),
    ) {
        SectionHeader(
            iconRes = R.drawable.ic_nightlight_24,
            headerRes = R.string.theme,
            modifier = Modifier.padding(bottom = 22.dp),
        )

        ThemeSelection(
            theme = widgetConfiguration.theme.collectAsStateWithLifecycle().value,
            customThemeSelected = widgetConfiguration.customThemeSelected.collectAsStateWithLifecycle().value,
            setTheme = { widgetConfiguration.theme.value = it },
            useDynamicColors = widgetConfiguration.useDynamicColors.collectAsStateWithLifecycle().value,
            setUseDynamicColors = { widgetConfiguration.useDynamicColors.value = it },
            getCustomColor = { widgetConfiguration.customColorsMap.getValue(it).toColor() },
            setCustomColor = { colorSection, color ->
                widgetConfiguration.customColorsMap[colorSection] = color.toArgb()
            }
        )

        SectionHeader(
            iconRes = R.drawable.ic_opacity_24,
            headerRes = R.string.opacity,
        )
        OpacitySliderWithLabel(
            opacity = widgetConfiguration.opacity.collectAsStateWithLifecycle().value,
            onOpacityChanged = {
                widgetConfiguration.opacity.value = it
            },
            modifier = Modifier.padding(horizontal = 6.dp),
        )

        SectionHeader(
            iconRes = R.drawable.ic_checklist_24,
            headerRes = R.string.properties,
        )
        PropertyCheckRows(
            rememberWidgetWifiPropertyCheckRowData(
                widgetConfiguration = widgetConfiguration,
                locationAccessState = locationAccessState
            ),
            showInfoDialog = showPropertyInfoDialog,
        )

        SectionHeader(
            iconRes = R.drawable.ic_gamepad_24,
            headerRes = R.string.buttons,
        )
        PropertyCheckRows(
            dataList = remember {
                WidgetButton.entries.map {
                    PropertyCheckRowData.fromMutableMap(
                        property = it,
                        isCheckedMap = widgetConfiguration.buttonMap
                    )
                }
                    .toPersistentList()
            }
        )

        SectionHeader(
            iconRes = com.w2sv.widget.R.drawable.ic_refresh_24,
            headerRes = R.string.refreshing,
        )
        val context: Context = LocalContext.current
        PropertyCheckRows(
            dataList = remember {
                persistentListOf(
                    PropertyCheckRowData.fromMutableMap(
                        property = WidgetRefreshingParameter.RefreshPeriodically,
                        isCheckedMap = widgetConfiguration.refreshingParametersMap,
                        infoDialogData = PropertyInfoDialogData(
                            title = context.getString(WidgetRefreshingParameter.RefreshPeriodically.labelRes),
                            description = context.getString(R.string.refresh_periodically_info)
                        ),
                        subPropertyCheckRowData = persistentListOf(
                            PropertyCheckRowData.fromMutableMap(
                                property = WidgetRefreshingParameter.RefreshOnLowBattery,
                                isCheckedMap = widgetConfiguration.refreshingParametersMap
                            )
                        )
                    ),
                    PropertyCheckRowData.fromMutableMap(
                        property = WidgetRefreshingParameter.DisplayLastRefreshDateTime,
                        isCheckedMap = widgetConfiguration.refreshingParametersMap
                    )
                )
            },
            showInfoDialog = showPropertyInfoDialog
        )
    }
}

@Composable
private fun rememberWidgetWifiPropertyCheckRowData(
    widgetConfiguration: UnconfirmedWidgetConfiguration,
    locationAccessState: LocationAccessState,
): ImmutableList<PropertyCheckRowData<WidgetWifiProperty>> {

    fun moreThanOneWifiPropertyChecked(): Boolean =
        widgetConfiguration.wifiProperties.values.count { it } > 1

    val widgetWifiPropertyShakeControllerMap: Map<WidgetWifiProperty, ShakeController> = remember {
        WidgetWifiProperty.entries.associateWith { ShakeController() }
    }

    val v4AndV6EnablementShakeControllerMap: Map<WidgetWifiProperty.IP.SubProperty, ShakeController> =
        remember {
            widgetConfiguration.ipSubProperties.keys
                .filter { it.isAddressTypeEnablementProperty }
                .associateWith { ShakeController() }
        }

    val context = LocalContext.current
    return remember {
        WidgetWifiProperty.entries
            .map { property ->
                val shakeController = widgetWifiPropertyShakeControllerMap.getValue(property)

                PropertyCheckRowData.fromMutableMap(
                    property = property,
                    isCheckedMap = widgetConfiguration.wifiProperties,
                    allowCheckChange = when (property) {
                        is WidgetWifiProperty.NonIP.LocationAccessRequiring -> { isCheckedNew ->
                            (if (isCheckedNew) {
                                locationAccessState.run {
                                    isGranted.also {
                                        if (!it) {
                                            launchRequest(
                                                LocationAccessPermissionRequestTrigger.PropertyCheckChange(
                                                    property,
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {
                                moreThanOneWifiPropertyChecked()
                            })
                                .also {
                                    if (!it) {
                                        shakeController.shake(shakeConfig)
                                    }
                                }
                        }

                        else -> { isCheckedNew ->
                            (isCheckedNew || moreThanOneWifiPropertyChecked()).also {
                                if (!it) {
                                    shakeController
                                        .shake(shakeConfig)
                                }
                            }
                        }
                    },
                    subPropertyCheckRowData = when (property) {
                        is WidgetWifiProperty.IP -> {
                            property.subProperties
                                .map { subProperty ->
                                    val subPropertyShakeController =
                                        v4AndV6EnablementShakeControllerMap[subProperty]

                                    PropertyCheckRowData.fromMutableMap(
                                        property = subProperty,
                                        isCheckedMap = widgetConfiguration.ipSubProperties,
                                        allowCheckChange = { newValue ->
                                            subProperty.allowCheckChange(
                                                newValue,
                                                widgetConfiguration.ipSubProperties
                                            )
                                                .also {
                                                    if (!it) {
                                                        subPropertyShakeController?.shake(
                                                            shakeConfig
                                                        )
                                                    }
                                                }
                                        },
                                        modifier = subPropertyShakeController?.let {
                                            Modifier.shake(
                                                it
                                            )
                                        }
                                            ?: Modifier
                                    )
                                }
                                .toPersistentList()
                        }

                        else -> persistentListOf()
                    },
                    infoDialogData = property.getInfoDialogData(context),
                    modifier = Modifier.shake(shakeController)
                )
            }
            .toPersistentList()
    }
}

private val shakeConfig = ShakeConfig(
    iterations = 2,
    translateX = 12.5f,
    stiffness = 20_000f
)

private fun WidgetWifiProperty.IP.SubProperty.allowCheckChange(
    newValue: Boolean,
    subPropertyEnablementMap: Map<WidgetWifiProperty.IP.SubProperty, Boolean>
): Boolean =
    when (val capturedKind = kind) {
        is WidgetWifiProperty.IP.V4AndV6.AddressTypeEnablement -> {
            newValue || subPropertyEnablementMap.getValue(
                WidgetWifiProperty.IP.SubProperty(
                    property = property,
                    kind = capturedKind.opposingAddressTypeEnablement
                )
            )
        }

        else -> true
    }

@Composable
private fun SectionHeader(
    @DrawableRes iconRes: Int,
    @StringRes headerRes: Int,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier.padding(vertical = 22.dp),
) {
    IconHeader(
        iconRes = iconRes,
        headerRes = headerRes,
        modifier = modifier.padding(horizontal = 16.dp),
    )
}
