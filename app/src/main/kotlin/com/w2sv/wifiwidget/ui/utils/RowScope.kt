package com.w2sv.wifiwidget.ui.utils

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RowScope.RightAligned(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Spacer(modifier = modifier.weight(1f))
    content()
}