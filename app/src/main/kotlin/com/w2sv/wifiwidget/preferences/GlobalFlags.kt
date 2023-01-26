package com.w2sv.wifiwidget.preferences

import android.content.SharedPreferences
import com.w2sv.typedpreferences.descendants.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalFlags @Inject constructor(sharedPreferences: SharedPreferences) :
    BooleanPreferences(
        "locationPermissionDialogShown" to false,
        sharedPreferences = sharedPreferences
    ) {
    var locationPermissionDialogShown by this
}