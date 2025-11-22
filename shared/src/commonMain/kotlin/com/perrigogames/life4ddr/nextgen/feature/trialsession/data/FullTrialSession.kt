package com.perrigogames.life4ddr.nextgen.feature.trialsession.data

import com.perrigogames.life4ddr.nextgen.db.TrialSession
import com.perrigogames.life4ddr.nextgen.db.TrialSong

data class FullTrialSession(
    val session: TrialSession,
    val songs: List<TrialSong>,
)