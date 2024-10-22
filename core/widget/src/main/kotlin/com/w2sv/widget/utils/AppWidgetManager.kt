package com.w2sv.widget.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.w2sv.androidutils.appwidget.getAppWidgetIds
import com.w2sv.widget.WidgetProvider

fun AppWidgetManager.getWifiWidgetIds(context: Context): IntArray =
    getWifiWidgetIds(context.packageName)

fun AppWidgetManager.getWifiWidgetIds(packageName: String): IntArray =
    getAppWidgetIds(packageName, WidgetProvider::class.java)

fun AppWidgetManager.attemptWifiWidgetPin(
    packageName: String,
    successCallback: PendingIntent?,
    onFailure: () -> Unit
) {
    if (isRequestPinAppWidgetSupported) {
        requestPinAppWidget(
            ComponentName(packageName, WidgetProvider::class.java.name),
            null,
            successCallback
        )
    } else {
        onFailure()
    }
}
