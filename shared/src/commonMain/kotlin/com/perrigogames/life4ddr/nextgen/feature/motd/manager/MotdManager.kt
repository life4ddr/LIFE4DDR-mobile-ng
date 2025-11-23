package com.perrigogames.life4ddr.nextgen.feature.motd.manager

import com.perrigogames.life4ddr.nextgen.api.base.unwrapLoaded
import com.perrigogames.life4ddr.nextgen.feature.motd.data.MessageOfTheDay
import com.perrigogames.life4ddr.nextgen.feature.motd.data.MotdLocalRemoteData
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import kotlin.getValue

interface MotdManager {
    val motdFlow: SharedFlow<Event?>
    val dataVersionString: Flow<String>
}

class DefaultMotdManager: MotdManager, BaseModel() {

    private val _motdFlow = MutableSharedFlow<Event?>(replay = 8)
    override val motdFlow: SharedFlow<Event?> = _motdFlow

    private val data: MotdLocalRemoteData by inject()
    private val settings: MotdSettings by inject()

    override val dataVersionString: Flow<String> =
        data.versionState.map { it.versionString }

    init {
        mainScope.launch {
            combine(
                data.dataState,
                data.versionState
            ) { data, version ->
                if (version.majorVersionBlocked) {
                    _motdFlow.emit(Event.DataRequiresAppUpdateEvent)
                } else if (settings.shouldShowMotd(version.version)) {
                    data.unwrapLoaded()?.let {
                        _motdFlow.emit(Event.MotdEvent(it))
                    }
                }
            }
        }
        mainScope.launch {
            data.start()
        }
    }
}

sealed class Event {
    data object DataRequiresAppUpdateEvent: Event()
    data class MotdEvent(val motd: MessageOfTheDay): Event()
}
