package com.w2sv.wifiwidget.ui.screens.home

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.BackPressHandler
import com.w2sv.androidutils.extensions.locationServicesEnabled
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.common.WifiProperty
import com.w2sv.common.extensions.getValueSynchronously
import com.w2sv.common.preferences.DataStoreRepository
import com.w2sv.common.preferences.PreferencesKey
import com.w2sv.widget.WidgetProvider
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.NonAppliedStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    val dataStoreRepository: DataStoreRepository,
    @ApplicationContext context: Context
) : androidx.lifecycle.ViewModel() {

    /**
     * In-App Theme
     */

    val inAppThemeState = NonAppliedStateFlow(
        viewModelScope,
        dataStoreRepository.inAppTheme
    ) {
        dataStoreRepository.saveEnum(PreferencesKey.IN_APP_THEME, it)
    }

    /**
     * Widget Pin Listening
     */

    fun onWidgetOptionsUpdated(widgetId: Int, context: Context) {
        if (widgetIds.add(widgetId)) {
            onNewWidgetPinned(widgetId, context)
        }
    }

    private fun onNewWidgetPinned(widgetId: Int, context: Context) {
        i { "Pinned new widget w ID=$widgetId" }
        context.showToast(R.string.pinned_widget)

        viewModelScope.launch {
            if (dataStoreRepository.wifiProperties.getValue(WifiProperty.SSID)
                    .first() && !context.locationServicesEnabled
            )
                context.showToast(
                    R.string.ssid_display_requires_location_services_to_be_enabled,
                    Toast.LENGTH_LONG
                )
        }
    }

    private val widgetIds: MutableSet<Int> =
        WidgetProvider.getWidgetIds(context).toMutableSet()

    /**
     * lap := Location Access Permission
     */

    val lapDialogAnswered: Boolean
        get() = dataStoreRepository.locationAccessPermissionDialogAnswered.getValueSynchronously()

    fun onLAPDialogAnswered() {
        viewModelScope.launch {
            dataStoreRepository.save(
                PreferencesKey.LOCATION_ACCESS_PERMISSION_DIALOG_ANSWERED,
                true
            )
        }
    }

    val lapDialogTrigger: MutableStateFlow<LocationAccessPermissionDialogTrigger?> =
        MutableStateFlow(null)

    val lapRequestTrigger: MutableStateFlow<LocationAccessPermissionDialogTrigger?> =
        MutableStateFlow(null)

    val lapRequestLaunchedAtLeastOnce: Boolean
        get() = dataStoreRepository.locationAccessPermissionRequestedAtLeastOnce.getValueSynchronously()

    /**
     * BackPress
     */

    val exitApplication = MutableSharedFlow<Unit>()

    fun onBackPress(context: Context) {
        backPressHandler.invoke(
            onFirstPress = {
                context.showToast(context.getString(R.string.tap_again_to_exit))
            },
            onSecondPress = {
                viewModelScope.launch {
                    exitApplication.emit(Unit)
                }
            }
        )
    }

    private val backPressHandler = BackPressHandler(viewModelScope, 2500L)
}