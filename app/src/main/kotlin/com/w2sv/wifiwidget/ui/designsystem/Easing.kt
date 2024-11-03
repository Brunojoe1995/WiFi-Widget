package com.w2sv.wifiwidget.ui.designsystem

import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import com.w2sv.composed.extensions.toEasing

object Easing {
    val overshoot = OvershootInterpolator().toEasing()
    val anticipate = AnticipateInterpolator().toEasing()
}
