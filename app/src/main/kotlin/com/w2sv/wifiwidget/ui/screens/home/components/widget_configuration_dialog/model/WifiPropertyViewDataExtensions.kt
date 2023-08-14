package com.w2sv.wifiwidget.ui.screens.home.components.widget_configuration_dialog.model

import com.w2sv.data.model.WifiProperty

val WifiProperty.ViewData.infoDialogData: PropertyInfoDialogData
    get() = PropertyInfoDialogData(
        labelRes,
        descriptionRes,
        learnMoreUrl
    )