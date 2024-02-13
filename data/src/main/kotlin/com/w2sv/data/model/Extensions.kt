package com.w2sv.data.model

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.DataStoreEntry
import com.w2sv.domain.model.WidgetBottomBarElement
import com.w2sv.domain.model.WidgetColorSection
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty

private val Any.preferencesKeyName: String
    get() = this::class.simpleName!!

internal val WidgetWifiProperty.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(preferencesKeyName),
        defaultValue = defaultIsEnabled,
    )

internal val WidgetWifiProperty.IP.SubProperty.isEnabledDse
    get() =
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("${property.preferencesKeyName}.${kind.preferencesKeyName}"),
            defaultValue = true,
        )

internal val WidgetBottomBarElement.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(
            when (this) {
                WidgetBottomBarElement.LastRefreshTimeDisplay -> "ShowDateTime"
                WidgetBottomBarElement.RefreshButton -> "WidgetButton.Refresh"
                WidgetBottomBarElement.GoToWidgetSettingsButton -> "WidgetButton.GoToWidgetSettings"
                WidgetBottomBarElement.GoToWifiSettingsButton -> "WidgetButton.GoToWifiSettings"
            }
        ),
        defaultValue = true,
    )

internal val WidgetColorSection.valueDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = intPreferencesKey(
            when (this) {
                WidgetColorSection.Background -> "Background"
                WidgetColorSection.Primary -> "Labels"
                WidgetColorSection.Secondary -> "Other"
            }
        ),
        defaultValue = defaultValue,
    )

internal val WidgetRefreshingParameter.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(
            when (this) {
                WidgetRefreshingParameter.RefreshOnLowBattery -> "RefreshOnBatteryLow"
                WidgetRefreshingParameter.RefreshPeriodically -> "RefreshPeriodically"
            }
        ),
        defaultValue = defaultIsEnabled,
    )