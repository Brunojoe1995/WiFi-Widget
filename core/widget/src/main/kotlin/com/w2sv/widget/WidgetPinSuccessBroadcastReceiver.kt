package com.w2sv.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.w2sv.common.di.AppDefaultScope
import com.w2sv.widget.di.MutableWidgetPinSuccessFlow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * To be invoked on successful widget pin triggered from within the app. Emits on [MutableWidgetPinSuccessFlow].
 */
@AndroidEntryPoint
internal class WidgetPinSuccessBroadcastReceiver : BroadcastReceiver() {

    @Inject
    @MutableWidgetPinSuccessFlow
    lateinit var mutableWidgetPinSuccessFlow: MutableSharedFlow<Unit>

    @Inject
    @AppDefaultScope
    lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        scope.launch { mutableWidgetPinSuccessFlow.emit(Unit) }
    }

    companion object {
        const val REQUEST_CODE = 1447
    }
}
