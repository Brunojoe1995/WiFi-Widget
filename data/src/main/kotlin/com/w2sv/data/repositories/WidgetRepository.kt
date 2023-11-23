package com.w2sv.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.data.model.isEnabledDSE
import com.w2sv.data.model.isV4EnabledDse
import com.w2sv.data.model.isV6EnabledDse
import com.w2sv.data.model.valueDSE
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.WidgetButton
import com.w2sv.domain.model.WidgetColor
import com.w2sv.domain.model.WidgetRefreshingParameter
import com.w2sv.domain.model.WidgetWifiProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepository @Inject constructor(
    dataStore: DataStore<Preferences>,
) : PreferencesDataStoreRepository(dataStore) {

    val optionsChangedWidgetId get() = _optionsChangedWidgetId.asSharedFlow()
    private val _optionsChangedWidgetId = MutableSharedFlow<Int>()

    fun onWidgetOptionsChanged(widgetId: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            _optionsChangedWidgetId.emit(widgetId)
        }
    }

    fun getEnabledWifiProperties(): Set<WidgetWifiProperty> =
        getWifiPropertyEnablementMap().getSynchronousMap().filterValues { it }.keys

    // ================
    // PersistedValue
    // ================

    val theme = getPersistedValue(intPreferencesKey("widgetTheme"), Theme.SystemDefault)

    val useDynamicColors =
        getPersistedValue(booleanPreferencesKey("widgetConfiguration.useDynamicColor"), false)

    val opacity = getPersistedValue(floatPreferencesKey("opacity"), 1.0f)

    // ================
    // Map
    // ================

    // TODO: look for singular save function

    fun getCustomColorsMap(): Map<WidgetColor, Flow<Int>> =
        getTypeToValueMap(WidgetColor.entries.associateBy { it.valueDSE })

    suspend fun saveCustomColorsMap(map: Map<WidgetColor, Int>) {
        saveMap(map.mapKeys { (k, _) -> k.valueDSE })
    }

    fun getWifiPropertyEnablementMap(): Map<WidgetWifiProperty, Flow<Boolean>> =
        getTypeToValueMap(WidgetWifiProperty.entries.associateBy { it.isEnabledDSE })

    suspend fun saveWifiPropertyEnablementMap(map: Map<WidgetWifiProperty, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    fun getEnabledAddressTypesMap(): Map<WidgetWifiProperty.IPProperty.V4AndV6, Flow<WidgetWifiProperty.IPProperty.V4AndV6.EnabledTypes>> =
        WidgetWifiProperty.IPProperty.V4AndV6.entries
            .associateWith {
                listOf(
                    it.isV4EnabledDse,
                    it.isV6EnabledDse
                )
            }
            .mapValues { (_, v) ->
                combine(v.map { getFlow(it) }) { (v4Enabled, v6Enabled) ->
                    WidgetWifiProperty.IPProperty.V4AndV6.EnabledTypes(
                        v4 = v4Enabled,
                        v6 = v6Enabled
                    )
                }
            }

    suspend fun saveEnabledAddressTypesMap(map: Map<WidgetWifiProperty.IPProperty.V4AndV6, WidgetWifiProperty.IPProperty.V4AndV6.EnabledTypes>) {
        map.forEach { (k, v) ->
            save(k.isV4EnabledDse.preferencesKey, v.v4)
            save(k.isV6EnabledDse.preferencesKey, v.v6)
        }
    }

    fun getRefreshingParametersEnablementMap(): Map<WidgetRefreshingParameter, Flow<Boolean>> {
        return getTypeToValueMap(WidgetRefreshingParameter.entries.associateBy { it.isEnabledDSE })
    }

    suspend fun saveRefreshingParametersEnablementMap(map: Map<WidgetRefreshingParameter, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    fun getButtonEnablementMap(): Map<WidgetButton, Flow<Boolean>> {
        return getTypeToValueMap(WidgetButton.entries.associateBy { it.isEnabledDSE })
    }

    suspend fun saveButtonEnablementMap(map: Map<WidgetButton, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    private fun <E, V> getTypeToValueMap(dseToType: Map<DataStoreEntry.UniType<V>, E>): Map<E, Flow<V>> =
        getFlowMap(dseToType.keys).mapKeys { (k, _) -> dseToType.getValue(k) }
}
