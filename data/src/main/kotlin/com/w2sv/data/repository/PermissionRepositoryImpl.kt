package com.w2sv.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.domain.repository.PermissionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionRepositoryImpl @Inject constructor(
    dataStore: DataStore<Preferences>,
) : PreferencesDataStoreRepository(dataStore),
    PermissionRepository {

    override val locationAccessPermissionRationalShown =
        getPersistedValue(booleanPreferencesKey("locationPermissionDialogAnswered"), false)

    override val locationAccessPermissionRequested = getPersistedValue(
        booleanPreferencesKey("locationAccessPermissionRequestedAtLeastOnce"),
        false
    )
}
