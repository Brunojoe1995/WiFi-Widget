package com.w2sv.wifiwidget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun diagonalGradient(lowerLeftCorner: Color, upperRightCorner: Color): Brush =
    Brush.linearGradient(
        listOf(
            lowerLeftCorner,
            upperRightCorner
        ),
        start = Offset(0f, Float.POSITIVE_INFINITY),
        end = Offset(Float.POSITIVE_INFINITY, 0f)
    )
