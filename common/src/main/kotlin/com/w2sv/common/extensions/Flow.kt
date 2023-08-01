package com.w2sv.common.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.w2sv.androidutils.coroutines.getValueSynchronously
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> Flow<T>.stateInWithInitial(scope: CoroutineScope, started: SharingStarted): StateFlow<T> =
    stateIn(scope = scope, started = started, initialValue = getValueSynchronously())

@Composable
fun <T : R, R> Flow<T>.collectAsStateWithInitial(context: CoroutineContext = EmptyCoroutineContext): State<R> =
    collectAsState(getValueSynchronously(), context)