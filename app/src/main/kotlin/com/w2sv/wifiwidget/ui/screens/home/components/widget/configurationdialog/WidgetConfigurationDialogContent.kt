package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog

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
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.IconHeader
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessState
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.OpacitySliderWithLabel
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.PropertyCheckRows
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.components.ThemeSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyInfoDialogData
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.UnconfirmedWidgetConfiguration
import com.w2sv.wifiwidget.ui.utils.toColor
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

private val verticalSectionHeaderPadding = 22.dp

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
            .padding(top = 8.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        val defaultSectionHeaderModifier = Modifier.padding(vertical = verticalSectionHeaderPadding)

        SectionHeader(
            iconRes = R.drawable.ic_nightlight_24,
            headerRes = R.string.theme,
            modifier = Modifier.padding(bottom = verticalSectionHeaderPadding),
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
            modifier = defaultSectionHeaderModifier
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
            modifier = defaultSectionHeaderModifier
        )
        PropertyCheckRows(
            dataList = rememberWidgetWifiPropertyCheckRowData(
                widgetConfiguration = widgetConfiguration,
                locationAccessState = locationAccessState
            ),
            showInfoDialog = showPropertyInfoDialog,
        )

        SectionHeader(
            iconRes = R.drawable.ic_gamepad_24,
            headerRes = R.string.buttons,
            modifier = defaultSectionHeaderModifier
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
            modifier = defaultSectionHeaderModifier
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
private fun SectionHeader(
    @DrawableRes iconRes: Int,
    @StringRes headerRes: Int,
    modifier: Modifier = Modifier,
) {
    IconHeader(
        iconRes = iconRes,
        headerRes = headerRes,
        modifier = modifier.padding(horizontal = 16.dp),
    )
}
