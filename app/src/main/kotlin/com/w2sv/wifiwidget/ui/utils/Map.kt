package com.w2sv.wifiwidget.ui.utils

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.w2sv.androidutils.coroutines.getSynchronousMap
import kotlinx.coroutines.flow.Flow

fun <K, V> Map<K, Flow<V>>.getSynchronousMutableStateMap(): SnapshotStateMap<K, V> =
    getSynchronousMap()
        .getMutableStateMap()

fun <K, V> Map<K, V>.getMutableStateMap(): SnapshotStateMap<K, V> =
    mutableStateMapOf<K, V>()
        .apply { putAll(this@getMutableStateMap) }
