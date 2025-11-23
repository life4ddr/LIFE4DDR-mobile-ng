package com.perrigogames.life4ddr.nextgen.feature.profile.manager

import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.enums.nullableNext
import com.perrigogames.life4ddr.nextgen.injectLogger
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import kotlinx.coroutines.flow.*
import org.koin.core.component.inject

/**
 * Manager class that deals with the current user's rank information.
 */
interface UserRankManager {
    val rank: StateFlow<LadderRank?>

    val targetRank: StateFlow<LadderRank?>

    fun setUserRank(rank: LadderRank?)

    fun setUserTargetRank(rank: LadderRank?)
}

class DefaultUserRankManager : BaseModel(), UserRankManager {

    private val ladderSettings: UserRankSettings by inject()
    private val logger by injectLogger("UserRankManager")

    override val rank: StateFlow<LadderRank?> = ladderSettings.rank
        .onEach { logger.v { "RANK: $it" } }
        .stateIn(mainScope, started = SharingStarted.Lazily, initialValue = null)

    override val targetRank: StateFlow<LadderRank?> = ladderSettings.targetRank
        .onEach { logger.v { "TARGET RANK: $it" } }
        .stateIn(mainScope, started = SharingStarted.Lazily, initialValue = null)

    override fun setUserRank(rank: LadderRank?) {
        ladderSettings.setRank(rank)
        ladderSettings.setTargetRank(rank.nullableNext)
    }

    override fun setUserTargetRank(rank: LadderRank?) {
        ladderSettings.setTargetRank(rank)
    }
}