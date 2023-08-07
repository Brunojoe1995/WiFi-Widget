package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.data.model.Theme
import com.w2sv.data.model.WidgetColor
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.ButtonColor
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.ThemeIndicatorProperties
import com.w2sv.wifiwidget.ui.components.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.components.drawer.UseDynamicColorsRow
import com.w2sv.wifiwidget.ui.components.drawer.dynamicColorsSupported
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LAPRequestTrigger
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.ButtonSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.OpacitySliderWithLabel
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.RefreshingParametersSelection
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.WifiPropertySelection
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components.colors.ColorSelection
import com.w2sv.wifiwidget.ui.theme.AppTheme
import com.w2sv.wifiwidget.ui.utils.circularTrifoldStripeBrush
import com.w2sv.wifiwidget.ui.utils.toColor
import com.w2sv.wifiwidget.ui.viewmodels.HomeScreenViewModel
import com.w2sv.wifiwidget.ui.viewmodels.WidgetViewModel

@Preview
@Composable
private fun Prev() {
    AppTheme {
        ConfigColumn()
    }
}

const val EPSILON = 1e-6f

@Composable
fun ConfigColumn(
    modifier: Modifier = Modifier,
    widgetConfigurationVM: WidgetViewModel = viewModel(),
    homeScreenVM: HomeScreenViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(scrollState)
    ) {
        val defaultSectionHeaderModifier = Modifier.padding(vertical = 22.dp)

        SectionHeader(
            titleRes = R.string.theme,
            iconRes = R.drawable.ic_nightlight_24,
            modifier = Modifier.padding(bottom = 22.dp)
        )

        val useDynamicColors by widgetConfigurationVM.useDynamicColors.collectAsState()
        val customThemeIndicatorWeight by animateFloatAsState(
            targetValue = if (useDynamicColors) EPSILON else 1f,
            label = ""
        )
        val theme by widgetConfigurationVM.theme.collectAsState()
        Row {
            ThemeSelectionRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ((1 - customThemeIndicatorWeight) * 32).dp),
                customThemeIndicatorProperties = ThemeIndicatorProperties(
                    theme = Theme.Custom,
                    labelRes = R.string.custom,
                    buttonColoring = ButtonColor.Gradient(
                        circularTrifoldStripeBrush(
                            widgetConfigurationVM.customColorsMap.getValue(WidgetColor.Background)
                                .toColor(),
                            widgetConfigurationVM.customColorsMap.getValue(WidgetColor.Primary)
                                .toColor(),
                            widgetConfigurationVM.customColorsMap.getValue(WidgetColor.Secondary)
                                .toColor()
                        )
                    )
                ),
                selected = theme,
                onSelected = {
                    widgetConfigurationVM.theme.value = it
                },
                themeWeights = mapOf(Theme.Custom to customThemeIndicatorWeight),
                themeIndicatorModifier = Modifier
                    .padding(horizontal = 12.dp)
                    .sizeIn(maxHeight = 92.dp)
            )
        }

        val customThemeSelected by remember {
            derivedStateOf {
                theme == Theme.Custom
            }
        }

        AnimatedVisibility(visible = customThemeSelected) {
            ColorSelection(
                widgetColors = widgetConfigurationVM.customColorsMap,
                modifier = Modifier
                    .padding(top = 18.dp)
            )
        }

        if (dynamicColorsSupported) {
            UseDynamicColorsRow(
                useDynamicColors = useDynamicColors,
                onToggleDynamicColors = {
                    widgetConfigurationVM.useDynamicColors.value = it
                    if (it && customThemeSelected) {
                        widgetConfigurationVM.theme.value = Theme.SystemDefault
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(top = 22.dp)
            )
        }

        SectionHeader(
            titleRes = R.string.opacity,
            iconRes = R.drawable.ic_opacity_24,
            modifier = defaultSectionHeaderModifier
        )
        OpacitySliderWithLabel(
            opacity = widgetConfigurationVM.opacity.collectAsState().value,
            onOpacityChanged = {
                widgetConfigurationVM.opacity.value = it
            },
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        SectionHeader(
            titleRes = R.string.properties,
            iconRes = R.drawable.ic_checklist_24,
            modifier = defaultSectionHeaderModifier
        )
        WifiPropertySelection(
            wifiPropertiesMap = widgetConfigurationVM.wifiProperties,
            subPropertiesMap = widgetConfigurationVM.subWifiProperties,
            allowLAPDependentPropertyCheckChange = { property, newValue ->
                when (newValue) {
                    true -> {
                        when (homeScreenVM.lapRationalShown) {
                            false -> {
                                homeScreenVM.lapRationalTrigger.value =
                                    LAPRequestTrigger.PropertyCheckChange(property)
                            }

                            true -> {
                                homeScreenVM.lapRequestTrigger.value =
                                    LAPRequestTrigger.PropertyCheckChange(property)
                            }
                        }
                        false
                    }

                    false -> true
                }
            },
            onInfoButtonClick = { widgetConfigurationVM.infoDialogProperty.value = it }
        )

        SectionHeader(
            titleRes = R.string.buttons,
            iconRes = R.drawable.ic_gamepad_24,
            modifier = defaultSectionHeaderModifier
        )
        ButtonSelection(widgetConfigurationVM.buttonMap)

        SectionHeader(
            titleRes = R.string.refreshing,
            iconRes = com.w2sv.widget.R.drawable.ic_refresh_24,
            modifier = defaultSectionHeaderModifier
        )
        RefreshingParametersSelection(
            widgetRefreshingMap = widgetConfigurationVM.refreshingParametersMap,
            scrollToContentColumnBottom = {
                with(scrollState) {
                    animateScrollTo(maxValue)
                }
            },
            onRefreshPeriodicallyInfoIconClick = {
                widgetConfigurationVM.refreshPeriodicallyInfoDialog.value = true
            }
        )
    }
}

@Composable
private fun SectionHeader(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.Center) {
            Icon(
                painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        Box(modifier = Modifier.weight(0.7f), contentAlignment = Alignment.Center) {
            JostText(
                text = stringResource(id = titleRes),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.weight(0.3f))
    }
}