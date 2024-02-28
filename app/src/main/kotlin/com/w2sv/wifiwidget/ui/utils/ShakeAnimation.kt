package com.w2sv.wifiwidget.ui.utils

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp

@Stable
class ShakeController(private val config: ShakeConfig) {
    fun shake() {
        trigger = System.currentTimeMillis()
    }

    internal suspend fun animate(animatable: Animatable<Float, *>) {
        for (i in 0..config.iterations) {
            animatable.animateTo(
                targetValue = if (i % 2 == 0) config.translateX else -config.translateX,
                animationSpec = spring(
                    stiffness = config.stiffness,
                )
            )
        }
        animatable.animateTo(0f)
        // Reset trigger, in case of the target getting readded to the composition, while controller persists
        trigger = null
    }

    internal var trigger by mutableStateOf<Long?>(null)
}

@Immutable
data class ShakeConfig(
    val iterations: Int,
    val translateX: Float,
    val stiffness: Float = Spring.StiffnessMedium,
)

@SuppressLint("ComposeModifierComposed")
fun Modifier.shake(controller: ShakeController) =
    this then composed {
        val animatable = remember(controller.trigger) { Animatable(0f) }

        LaunchedEffect(controller.trigger) {
            if (controller.trigger != null) {
                controller.animate(animatable)
            }
        }

        offset(x = animatable.value.dp)
    }