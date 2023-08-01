package com.w2sv.data.model

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry

enum class WidgetColorSection(
    override val preferencesKey: Preferences.Key<Int>,
    override val defaultValue: Int
) : DataStoreEntry.UniType<Int> {

    Background(intPreferencesKey("Background"), -9430904),  // Purplish
    Labels(intPreferencesKey("Labels"), -65536),  // Red
    Other(intPreferencesKey("Other"), -1)  // White
}