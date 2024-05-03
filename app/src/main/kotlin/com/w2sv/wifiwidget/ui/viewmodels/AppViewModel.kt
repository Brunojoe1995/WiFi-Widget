package com.w2sv.wifiwidget.ui.viewmodels

import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PermissionRepository
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.wifiwidget.di.MakeSnackbarVisualsFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    @MakeSnackbarVisualsFlow makeSnackbarVisualsFlow: MutableSharedFlow<(Context) -> SnackbarVisuals>,
    private val permissionRepository: PermissionRepository,
    private val preferencesRepository: PreferencesRepository
) :
    ViewModel() {

    val makeSnackbarVisualsFlow = makeSnackbarVisualsFlow.asSharedFlow()

    val theme = preferencesRepository.inAppTheme.stateIn(
        viewModelScope,
        SharingStarted.Eagerly
    )

    val useDynamicColors = preferencesRepository.useDynamicTheme.stateIn(
        viewModelScope,
        SharingStarted.Eagerly
    )

    fun saveTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesRepository.inAppTheme.save(theme)
        }
    }

    fun saveUseDynamicColors(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.useDynamicTheme.save(value)
        }
    }

    // ===============
    // Permissions
    // ===============

    val locationAccessPermissionRequested by permissionRepository::locationAccessPermissionRequested

    fun saveLocationAccessPermissionRequested() {
        viewModelScope.launch { permissionRepository.locationAccessPermissionRequested.save(true) }
    }

    val locationAccessRationalShown by permissionRepository::locationAccessPermissionRationalShown

    fun saveLocationAccessRationalShown() {
        viewModelScope.launch { permissionRepository.locationAccessPermissionRationalShown.save(true) }
    }
}
