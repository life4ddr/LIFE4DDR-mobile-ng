package com.perrigogames.life4ddr.nextgen.feature.trials.view

import com.perrigogames.life4ddr.nextgen.db.SelectFullSessions
import com.perrigogames.life4ddr.nextgen.feature.trials.data.Course
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialState
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialJacketCorner
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import dev.icerock.moko.resources.desc.StringDesc

data class UITrialJacket(
    val trial: Course,
    val session: SelectFullSessions? = null,
    val overrideCorner: TrialJacketCorner? = null,
    val rank: TrialRank? = null,
    val exScore: StringDesc? = null,
    val tintOnRank: TrialRank? = null,
    val showExRemaining: Boolean = false,
) {

    val viewAlpha: Float = if (trial.state == TrialState.RETIRED) 0.5f else 1f

    val cornerType: TrialJacketCorner = overrideCorner ?: when {
        trial.state == TrialState.NEW && session == null -> TrialJacketCorner.NEW
        trial is Course.Event -> TrialJacketCorner.EVENT
        else -> TrialJacketCorner.NONE
    }

    val shouldTint = rank != null && rank == tintOnRank
}