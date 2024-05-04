package com.w2sv.wifiwidget.ui.shared_viewmodels

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.material3.SnackbarVisuals
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.ui.reversible_state.ReversibleStateFlow
import com.w2sv.androidutils.ui.reversible_state.ReversibleStateMap
import com.w2sv.common.di.PackageName
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.widget.WidgetDataRefreshWorker
import com.w2sv.widget.WidgetProvider
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.WidgetPinSuccessBroadcastReceiver
import com.w2sv.wifiwidget.di.MakeSnackbarVisualsFlow
import com.w2sv.wifiwidget.di.WidgetPinSuccessFlow
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.model.ReversibleWidgetConfiguration
import com.w2sv.wifiwidget.ui.utils.fromDataStoreFlowMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    private val repository: WidgetRepository,
    private val widgetDataRefreshWorkerManager: WidgetDataRefreshWorker.Manager,
    private val appWidgetManager: AppWidgetManager,
    @PackageName private val packageName: String,
    @MakeSnackbarVisualsFlow private val sharedSnackbarVisuals: MutableSharedFlow<(Context) -> SnackbarVisuals>,
    @ApplicationContext context: Context,
    @WidgetPinSuccessFlow widgetPinSuccessFlow: MutableSharedFlow<Unit>
) :
    ViewModel() {

    // =========
    // Pinning
    // =========

    val widgetPinSuccessFlow = widgetPinSuccessFlow.asSharedFlow()

    fun attemptWidgetPin(context: Context) {
        appWidgetManager.attemptWifiWidgetPin(
            packageName = packageName,
            successCallback = PendingIntent.getBroadcast(
                context,
                WidgetPinSuccessBroadcastReceiver.REQUEST_CODE,
                Intent(context, WidgetPinSuccessBroadcastReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE
            ),
            onFailure = {
                viewModelScope.launch {
                    sharedSnackbarVisuals.emit {
                        AppSnackbarVisuals(
                            msg = it.getString(R.string.widget_pinning_not_supported_by_your_device_launcher),
                            kind = SnackbarKind.Error
                        )
                    }
                }
            }
        )
    }

    // =========
    // Configuration
    // =========

    private val refreshInterval = ReversibleStateFlow(
        scope = viewModelScope,
        dataStoreFlow = repository.refreshInterval,
        started = SharingStarted.Eagerly
    )

    val configuration = ReversibleWidgetConfiguration(
        coloringConfig = ReversibleStateFlow(
            scope = viewModelScope,
            appliedState = repository.coloringConfig.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = WidgetColoring.Config()
            ),
            syncState = { repository.saveColoringConfig(it) }
        ),
        opacity = ReversibleStateFlow(
            scope = viewModelScope,
            dataStoreFlow = repository.opacity,
            started = SharingStarted.Eagerly
        ),
        fontSize = ReversibleStateFlow(
            scope = viewModelScope,
            dataStoreFlow = repository.fontSize,
            started = SharingStarted.Eagerly
        ),
        wifiProperties = ReversibleStateMap.fromDataStoreFlowMap(
            scope = viewModelScope,
            dataStoreFlowMap = repository.wifiPropertyEnablementMap,
        ),
        ipSubProperties = ReversibleStateMap.fromDataStoreFlowMap(
            scope = viewModelScope,
            dataStoreFlowMap = repository.ipSubPropertyEnablementMap,
        ),
        bottomRowMap = ReversibleStateMap.fromDataStoreFlowMap(
            scope = viewModelScope,
            dataStoreFlowMap = repository.bottomRowElementEnablementMap,
        ),
        refreshInterval = refreshInterval,
        refreshingParametersMap = ReversibleStateMap.fromDataStoreFlowMap(
            scope = viewModelScope,
            dataStoreFlowMap = repository.refreshingParametersEnablementMap,
            onStateSynced = {
                widgetDataRefreshWorkerManager.applyRefreshingSettings(
                    parameters = it,
                    interval = refreshInterval.value
                )
            }
        ),
        scope = viewModelScope,
        onStateSynced = {
            WidgetProvider.triggerDataRefresh(context)
            delay(500)  // To allow fab buttons to disappear before emission of snackbar
            sharedSnackbarVisuals.emit {
                AppSnackbarVisuals(
                    msg = it.getString(R.string.updated_widget_configuration),
                    kind = SnackbarKind.Success,
                )
            }
        },
    )
}