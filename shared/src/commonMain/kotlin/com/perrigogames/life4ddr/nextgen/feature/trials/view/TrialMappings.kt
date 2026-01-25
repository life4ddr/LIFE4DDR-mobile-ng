package com.perrigogames.life4ddr.nextgen.feature.trials.view

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.db.SelectFullSessions
import com.perrigogames.life4ddr.nextgen.feature.trials.data.Trial
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

fun Trial.toUIJacket(bestSession: SelectFullSessions?) = UITrialJacket(
    trial = this,
    session = bestSession,
    rank = bestSession?.goalRank,
    exScore = bestSession?.exScore?.let { StringDesc.ResourceFormatted(MR.strings.ex_score_string_format, it) },
    tintOnRank = TrialRank.entries.last(),
    showExRemaining = false,
)