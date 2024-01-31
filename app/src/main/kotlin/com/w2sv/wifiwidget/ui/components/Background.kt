package com.w2sv.wifiwidget.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier

@SuppressLint("ComposeComposableModifier")
@ReadOnlyComposable
@Composable
fun Modifier.nestedListBackground(): Modifier =
    this then Modifier
        .background(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium
        )