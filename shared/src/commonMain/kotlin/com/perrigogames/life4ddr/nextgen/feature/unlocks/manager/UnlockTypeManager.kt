package com.perrigogames.life4ddr.nextgen.feature.unlocks.manager

import com.perrigogames.life4ddr.nextgen.api.base.unwrapLoaded
import com.perrigogames.life4ddr.nextgen.feature.unlocks.data.UnlockType
import com.perrigogames.life4ddr.nextgen.feature.unlocks.data.UnlockTypesRemoteData
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

interface UnlockTypeManager {
    val typesFlow: StateFlow<List<UnlockType>>
    val basicLockKeys: StateFlow<List<Int>>
    val expandedLockKeys: StateFlow<List<Int>>
    fun findByKey(key: Int?): UnlockType?
}

class DefaultUnlockTypeManager(
    private val data: UnlockTypesRemoteData
) : UnlockTypeManager, BaseModel(), KoinComponent {

    private val _typesFlow = MutableStateFlow<List<UnlockType>>(emptyList())
    override val typesFlow: StateFlow<List<UnlockType>> = _typesFlow.asStateFlow()

    override val basicLockKeys: StateFlow<List<Int>>
        get() = typesFlow
            .map { it.map { it.key }}
            .stateIn(mainScope, SharingStarted.Lazily, emptyList())
    override val expandedLockKeys: StateFlow<List<Int>>
        get() = typesFlow
            .map { it.filter { it.isExpanded }.map { it.key }}
            .stateIn(mainScope, SharingStarted.Lazily, emptyList())

    override fun findByKey(key: Int?): UnlockType? = _typesFlow.value.firstOrNull { it.key == key }

    init {
        mainScope.launch {
            data.dataState
                .unwrapLoaded()
                .filterNotNull()
                .map { it.types }
                .collect { _typesFlow.value = it }
        }
        mainScope.launch {
            data.start()
        }
    }
}