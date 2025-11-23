package com.perrigogames.life4ddr.nextgen.model

import com.perrigogames.life4ddr.nextgen.injectLogger
import org.koin.core.component.inject

enum class MajorUpdate {
    BASE_VERSION
}

class MajorUpdateManager: BaseModel() {

    private val logger by injectLogger("MajorUpdate")
    private val settings: MajorUpdateSettings by inject()

    val updates: List<MajorUpdate> by lazy {
        val currentUpdate = settings.lastMajorUpdateSeen
        val out = MajorUpdate.entries.filter { it.ordinal > currentUpdate }
        out.forEach { logger.i("Processing upgrade ${it.name}") }
        settings.lastMajorUpdateSeen = MajorUpdate.entries.last().ordinal
        out
    }
}
