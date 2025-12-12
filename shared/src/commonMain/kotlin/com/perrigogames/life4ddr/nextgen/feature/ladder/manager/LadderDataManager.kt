package com.perrigogames.life4ddr.nextgen.feature.ladder.manager

import com.perrigogames.life4ddr.nextgen.api.base.unwrapLoaded
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderRankData
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderRemoteData
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderVersion
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.RankEntry
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import kotlin.getValue

/**
 * Manager class that deals with determining the correct ladder data to serve based on
 * the selected game version.
 */
class LadderDataManager(
    ladderSettings: LadderSettings,
    private val data: LadderRemoteData
): BaseModel() {

    val dataVersionString: Flow<String> =
        data.versionState.map { it.versionString }

    private val _ladderData: Flow<LadderRankData?> =
        data.dataState.unwrapLoaded()

    private val _ladderDataForGameVersion: Flow<LadderVersion?> =
        combine(
            _ladderData.filterNotNull(),
            ladderSettings.selectedGameVersion
        ) { ladderData, selectedVersion ->
            ladderData.gameVersions[selectedVersion]
        }

    init {
        mainScope.launch {
            data.start()
        }
    }

    fun requirementsForRank(rank: LadderRank?): Flow<RankEntry?> =
        _ladderDataForGameVersion.map {
            it?.rankRequirements?.firstOrNull { reqs -> reqs.rank == rank }
        }

    val unlockRequirement: Flow<LadderRank?> =
        _ladderDataForGameVersion.map { it?.unlockRequirement }
}
