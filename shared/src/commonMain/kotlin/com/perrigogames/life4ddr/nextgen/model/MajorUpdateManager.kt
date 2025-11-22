package com.perrigogames.life4ddr.nextgen.model

import org.koin.core.component.inject

// TODO Logger
// TODO Settings

enum class MajorUpdate {
    BASE_VERSION
}

class MajorUpdateManager: BaseModel() {

//    private val logger: Logger by injectLogger("MajorUpdate")
//    private val settings: Settings by inject()

    val updates: List<MajorUpdate> by lazy {
        val currentUpdate = -1
//        val currentUpdate = settings.getInt(KEY_MAJOR_UPDATE, -1)
        val out = MajorUpdate.entries.filter { it.ordinal > currentUpdate }
//        out.forEach { logger.i("Processing upgrade ${it.name}") }
//        settings[KEY_MAJOR_UPDATE] = MajorUpdate.entries.last().ordinal
        out
    }
}
