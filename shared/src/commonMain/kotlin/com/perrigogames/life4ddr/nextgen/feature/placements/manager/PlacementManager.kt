package com.perrigogames.life4ddr.nextgen.feature.placements.manager

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.PLACEMENTS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.base.LocalUncachedDataReader
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacement
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacementListScreen
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacementSkipConfirmation
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementListInput
import com.perrigogames.life4ddr.nextgen.feature.songlist.manager.SongDataManager
import com.perrigogames.life4ddr.nextgen.feature.trials.data.Trial
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialData
import com.perrigogames.life4ddr.nextgen.feature.trials.view.toUITrialSong
import com.perrigogames.life4ddr.nextgen.injectLogger
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlin.math.max
import kotlin.math.min

class PlacementManager: BaseModel() {

    private val songDataManager: SongDataManager by inject()
    private val json: Json by inject()
    private val dataReader: LocalUncachedDataReader by inject(named(PLACEMENTS_FILE_NAME))
    private val logger by injectLogger("PlacementManager")

    private val baseData: List<Trial> = json
        .decodeFromString(TrialData.serializer(), dataReader.loadInternalString())
        .trials

    private val _placements = songDataManager.libraryFlow
        .map { library ->
            logger.v { "library=${library.songs.keys.size}" }
            if (library.songs.isEmpty()) return@map emptyList()

            baseData.forEach { placement ->
                placement.songs.forEach { songEntry ->
                    val song = library.songs.keys.firstOrNull { it.skillId == songEntry.skillId }
                    logger.v { "song=${song.toString()}" }
                    val chart = song?.let { song ->
                        library.songs[song]?.firstOrNull {
                            it.difficultyClass == songEntry.difficultyClass &&
                                    it.playStyle == it.playStyle
                        }
                    }
                    logger.v { "chart=${chart.toString()}" }
                    chart?.let { songEntry.chart = it }
                }
            }
            baseData
        }
    val placements: StateFlow<List<Trial>> = _placements
        .stateIn(mainScope, SharingStarted.Lazily, emptyList())

    fun findPlacement(id: String) = _placements.map { placements ->
        placements.firstOrNull { it.id == id }
    }

    fun createUiData() = createUiData(
        placements = placements.value
    )

    fun createUiData(placements: List<Trial>) = UIPlacementListScreen(
        titleText = MR.strings.placements.desc(),
        headerText = MR.strings.placement_list_description.desc(),
        placements = placements.map { placement ->
            UIPlacement(
                id = placement.id,
                rankIcon = placement.placementRank!!.toLadderRank(),
                difficultyRangeString = placement.songs.let { songs ->
                    var lowest = songs[0].chart.difficultyNumber
                    var highest = songs[0].chart.difficultyNumber
                    songs.forEach { song ->
                        lowest = min(lowest, song.chart.difficultyNumber)
                        highest = max(highest, song.chart.difficultyNumber)
                    }
                    "L$lowest-L$highest" // FIXME resource
                },
                songs = placement.songs.map { it.toUITrialSong() },
                selectedInput = PlacementListInput.PlacementSelected(placement.id)
            )
        },
        ranksButtonText = MR.strings.select_rank_instead.desc(),
        ranksButtonInput = PlacementListInput.GoToRanksScreen,
        skipButtonText = MR.strings.start_no_rank.desc(),
        skipButtonInput = PlacementListInput.SkipPlacement,
    )
}
