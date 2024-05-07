package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model

import android.content.Context
import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.WidgetWifiProperty

@Immutable
data class InfoDialogData(
    val title: String,
    val description: String,
    val learnMoreUrl: String? = null,
)

fun WidgetWifiProperty.toInfoDialogData(context: Context): InfoDialogData =
    InfoDialogData(
        title = context.getString(labelRes),
        description = context.getString(descriptionRes),
        learnMoreUrl = learnMoreUrl,
    )
