package com.w2sv.wifiwidget.ui

import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.w2sv.kotlinutils.extensions.valueEqualTo
import com.w2sv.wifiwidget.extensions.getMutableStateMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class NonAppliedState<T>(
    protected val applyState: (T) -> Unit
) {
    val requiringUpdate = MutableStateFlow(false)
    abstract val value: T

    fun apply() {
        applyState(value)
        requiringUpdate.value = false
    }

    abstract fun reset()
}

class NonAppliedSnapshotStateMap<K, V>(
    private val getAppliedState: () -> Map<K, V>,
    updateAppliedState: (Map<K, V>) -> Unit,
    private val map: SnapshotStateMap<K, V> = getAppliedState().getMutableStateMap()
) : NonAppliedState<Map<K, V>>(updateAppliedState),
    MutableMap<K, V> by map {

    override val value: Map<K, V> get() = this

    override fun reset() {
        putAll(getAppliedState())
        requiringUpdate.value = false
    }

    override fun put(key: K, value: V): V? {
        val previous = map.put(key, value)
        requiringUpdate.value = !valueEqualTo(getAppliedState())
        return previous
    }
}

class NonAppliedStateFlow<T>(
    coroutineScope: CoroutineScope,
    private val dataStoreFlow: Flow<T>,
    updateAppliedState: (T) -> Unit,
    private val getAppliedState: () -> T = {
        runBlocking {
            dataStoreFlow.first()
        }
    }
) : NonAppliedState<T>(updateAppliedState),
    MutableStateFlow<T> by MutableStateFlow(getAppliedState()) {

    init {
        coroutineScope.launch {
            collect {
                requiringUpdate.value = it != getAppliedState()
            }
        }
    }

    override fun reset() {
        value = getAppliedState()
    }
}

class CoherentNonAppliedStates(
    vararg nonAppliedState: NonAppliedState<*>,
    coroutineScope: CoroutineScope
) : List<NonAppliedState<*>> by nonAppliedState.asList() {

    val requiringUpdate = MutableStateFlow(false)

    init {
        forEach {
            coroutineScope.launch {
                it.requiringUpdate.collect {
                    requiringUpdate.value = any { it.requiringUpdate.value }
                }
            }
        }
    }

    fun apply() {
        forEach {
            it.apply()
        }
    }

    fun reset() {
        forEach {
            it.reset()
        }
    }
}