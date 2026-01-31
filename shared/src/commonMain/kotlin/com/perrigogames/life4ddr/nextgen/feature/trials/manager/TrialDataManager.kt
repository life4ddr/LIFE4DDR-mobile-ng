package com.perrigogames.life4ddr.nextgen.feature.trials.manager

import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.AppInfo
import com.perrigogames.life4ddr.nextgen.api.base.CompositeData
import com.perrigogames.life4ddr.nextgen.feature.songlist.manager.SongDataManager
import com.perrigogames.life4ddr.nextgen.feature.trials.data.Course
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialRemoteData
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import com.russhwolf.settings.Settings
import dev.icerock.moko.mvvm.flow.cMutableStateFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Manages data relating to Trials.  This includes:
 * - the Trials themselves
 * - the current Trial in progress (the 'session')
 * - records for Trials the player has previously completed ('records')
 */
interface TrialDataManager {
    val trialsFlow: StateFlow<List<Course>>
    val dataVersionString: Flow<String>
    val trials: List<Course>
    val hasEventTrial: Boolean
    fun findTrial(id: String): Course?
}

class DefaultTrialDataManager(
    private val appInfo: AppInfo,
    private val settings: Settings,
    private val songDataManager: SongDataManager,
    private var data: TrialRemoteData,
    private val logger: Logger? = null
): BaseModel(), TrialDataManager {

    override val dataVersionString: Flow<String> =
        data.versionState.map { it.versionString }

    override val trials get() = trialsFlow.value
    override val hasEventTrial get() = trials.count { it is Course.Event && it.isActiveEvent } > 0

    private val _trialsFlow = MutableStateFlow<List<Course>>(emptyList()).cMutableStateFlow()
    override val trialsFlow: StateFlow<List<Course>> = _trialsFlow

    init {
        validateTrials()

        mainScope.launch {
            data.dataState
                .mapNotNull { (it as? CompositeData.LoadingState.Loaded)?.data?.trials }
                .onEach { trials ->
                    trials.forEach { trial ->
                        trial.songs.forEach { song ->
                            song.chart = songDataManager.getChart(
                                skillId = song.skillId,
                                playStyle = song.playStyle,
                                difficultyClass = song.difficultyClass,
                            ) ?: throw IllegalStateException("Chart not found for ${song.skillId}, ${song.playStyle}, ${song.difficultyClass}")
                        }
                    }
                }
                .collect(_trialsFlow)
        }
        mainScope.launch {
            data.start()
        }
    }

    private fun validateTrials() = trials.forEach { trial ->
        var sum = 0
        trial.songs.forEach { sum += it.ex }
        if (sum != trial.totalEx) {
            if (!appInfo.isDebug) {
                logger?.e { "Trial ${trial.name} has improper EX values: total_ex=${trial.totalEx}, sum=$sum" }
            }
        }
    }

    override fun findTrial(id: String) = trials.firstOrNull { it.id == id }
}
