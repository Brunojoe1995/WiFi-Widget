package com.w2sv.datastore.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.preferences_datastore.DataStoreEntry
import com.w2sv.androidutils.datastorage.preferences_datastore.PreferencesDataStoreRepository
import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreFlow
import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreFlowMap
import com.w2sv.datastore.proto.widget_coloring.WidgetColoringDataSource
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.WidgetBottomRowElement
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.repository.WidgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Singleton
class WidgetRepositoryImpl @Inject constructor(
    dataStore: DataStore<Preferences>,
    private val widgetColoringDataSource: WidgetColoringDataSource
) : PreferencesDataStoreRepository(dataStore),
    WidgetRepository {

    override val coloringConfig: Flow<WidgetColoring.Config> = widgetColoringDataSource.config

    override suspend fun saveColoringConfig(config: WidgetColoring.Config) {
        widgetColoringDataSource.saveConfig(config)
    }

    override val opacity = dataStoreFlow(
        key = floatPreferencesKey("opacity"),
        default = 1.0f,
    )

    override val fontSize = dataStoreFlow(
        key = intPreferencesKey("fontSize"),
        default = FontSize.Small,
    )

    // ================
    // Maps
    // ================

    override val wifiPropertyEnablementMap: DataStoreFlowMap<WidgetWifiProperty, Boolean> =
        dataStoreFlowMap(WidgetWifiProperty.entries.associateWith { it.isEnabledDSE })

    override val ipSubPropertyEnablementMap: DataStoreFlowMap<WidgetWifiProperty.IP.SubProperty, Boolean> =
        dataStoreFlowMap(
            WidgetWifiProperty.IP.entries
                .flatMap { it.subProperties }
                .associateWith { it.isEnabledDse }
        )

    override val bottomRowElementEnablementMap: DataStoreFlowMap<WidgetBottomRowElement, Boolean> =
        dataStoreFlowMap(WidgetBottomRowElement.entries.associateWith { it.isEnabledDSE })

    override val refreshingParametersEnablementMap: DataStoreFlowMap<WidgetRefreshingParameter, Boolean> =
        dataStoreFlowMap(WidgetRefreshingParameter.entries.associateWith { it.isEnabledDSE })

    override val refreshInterval: DataStoreFlow<Duration> =
        DataStoreFlow(
            flow = getFlow(
                preferencesKey = intPreferencesKey("refreshInterval"),
                defaultValue = 15
            )
                .map { it.minutes },
            default = 15.minutes,
            save = { save(intPreferencesKey("refreshInterval"), it.inWholeMinutes.toInt()) }
        )
}

private val WidgetWifiProperty.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(preferencesKeyName),
        defaultValue = defaultIsEnabled,
    )

private val WidgetWifiProperty.IP.SubProperty.isEnabledDse
    get() =
        DataStoreEntry.UniType.Impl(
            preferencesKey = booleanPreferencesKey("${property.preferencesKeyName}.${kind.preferencesKeyName}"),
            defaultValue = true,
        )

private val WidgetBottomRowElement.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(
            when (this) {
                WidgetBottomRowElement.LastRefreshTimeDisplay -> "ShowDateTime"
                WidgetBottomRowElement.RefreshButton -> "WidgetButton.Refresh"
                WidgetBottomRowElement.GoToWidgetSettingsButton -> "WidgetButton.GoToWidgetSettings"
                WidgetBottomRowElement.GoToWifiSettingsButton -> "WidgetButton.GoToWifiSettings"
            }
        ),
        defaultValue = true,
    )

private val WidgetRefreshingParameter.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(
            when (this) {
                WidgetRefreshingParameter.RefreshOnLowBattery -> "RefreshOnBatteryLow"
                WidgetRefreshingParameter.RefreshPeriodically -> "RefreshPeriodically"
            }
        ),
        defaultValue = defaultIsEnabled,
    )

private val Any.preferencesKeyName: String
    get() = this::class.simpleName!!