package com.w2sv.wifiwidget.ui.screens.home.widgetconfiguration.configcolumn

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.androidutils.extensions.requireCastActivity
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.common.CustomizableTheme
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.HomeActivity
import com.w2sv.wifiwidget.ui.screens.home.LocationAccessPermissionDialogTrigger
import com.w2sv.wifiwidget.ui.shared.JostText
import com.w2sv.wifiwidget.ui.shared.ThemeSelectionRow
import com.w2sv.wifiwidget.ui.shared.WifiWidgetTheme

@Preview
@Composable
private fun Prev() {
    WifiWidgetTheme {
        StatefulConfigColumn()
    }
}

@Composable
fun StatefulConfigColumn(
    modifier: Modifier = Modifier,
    viewModel: HomeActivity.ViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val scrollState = rememberScrollState()

    val theme by viewModel.widgetThemeState.collectAsState()
    val opacity by viewModel.widgetOpacityState.collectAsState()

    val context = LocalContext.current
    val lapRequestLauncher = context.requireCastActivity<HomeActivity>().lapRequestLauncher

    ConfigColumn(
        scrollState = scrollState,
        modifier = modifier,
        selectedTheme = {
            theme
        },
        onSelectedTheme = {
            viewModel.widgetThemeState.value = it
        },
        opacity = {
            opacity
        },
        onOpacityChanged = {
            viewModel.widgetOpacityState.value = it
        },
        propertyChecked = { property ->
            viewModel.widgetPropertyStateMap.map.getValue(property)
        },
        onCheckedChange = { property, value ->
            when {
                property == "SSID" && value -> {
                    when (viewModel.lapDialogAnswered) {
                        false -> viewModel.lapDialogTrigger.value =
                            LocationAccessPermissionDialogTrigger.SSIDCheck

                        true -> lapRequestLauncher.requestPermissionAndSetSSIDFlagCorrespondingly(
                            viewModel
                        )
                    }
                }

                else -> viewModel.confirmAndSyncPropertyChange(property, value) {
                    context.showToast(R.string.uncheck_all_properties_toast)
                }
            }
        },
        onInfoButtonClick = {
            viewModel.propertyInfoDialogIndex.value = it
        }
    )
}

@Composable
internal fun ConfigColumn(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    selectedTheme: () -> CustomizableTheme,
    onSelectedTheme: (CustomizableTheme) -> Unit,
    opacity: () -> Float,
    onOpacityChanged: (Float) -> Unit,
    propertyChecked: (String) -> Boolean,
    onCheckedChange: (String, Boolean) -> Unit,
    onInfoButtonClick: (Int) -> Unit,
    viewModel: HomeActivity.ViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val showCustomColorSection: Boolean by viewModel.showCustomThemeSection.collectAsState(false)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(vertical = 16.dp)
            .verticalScroll(scrollState)
    ) {
        val checkablePropertiesColumnModifier = Modifier.padding(horizontal = 26.dp)
        val defaultSectionHeaderModifier = Modifier.padding(vertical = 22.dp)

        SectionHeader(
            R.string.theme,
            R.drawable.ic_nightlight_24,
            Modifier.padding(top = 12.dp, bottom = 22.dp)
        )
        ThemeSelectionRow(
            modifier = Modifier.fillMaxWidth(),
            selected = selectedTheme,
            onSelected = onSelectedTheme
        )

        if (showCustomColorSection) {
            SectionHeader(titleRes = R.string.custom_theme, iconRes = R.drawable.ic_nightlight_24)
        }

        SectionHeader(
            R.string.opacity,
            R.drawable.ic_opacity_24,
            defaultSectionHeaderModifier
        )
        OpacitySliderWithValue(opacity = opacity, onOpacityChanged = onOpacityChanged)

        SectionHeader(
            R.string.properties,
            R.drawable.ic_checklist_24,
            defaultSectionHeaderModifier
        )
        PropertySelectionSection(
            modifier = checkablePropertiesColumnModifier,
            propertyChecked = propertyChecked,
            onCheckedChange = onCheckedChange,
            onInfoButtonClick = onInfoButtonClick
        )

        SectionHeader(
            titleRes = R.string.refreshing,
            iconRes = com.w2sv.widget.R.drawable.ic_refresh_24,
            modifier = defaultSectionHeaderModifier
        )
        RefreshingSection(checkablePropertiesColumnModifier) {
            with(scrollState) {
                animateScrollTo(maxValue)
            }
        }
    }
}

@Composable
private fun SectionHeader(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.Center) {
            Icon(
                painterResource(id = iconRes),
                contentDescription = "@null",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
            JostText(
                text = stringResource(id = titleRes),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.weight(0.6f))
    }
}