package com.perrigogames.life4ddr.nextgen.feature.ladder.manager

import com.perrigogames.life4ddr.nextgen.enums.GameVersion
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderSettings.Companion.KEY_GAME_VERSION
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderSettings.Companion.KEY_USE_MONOSPACE_SCORE
import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsManager
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface LadderSettings {
    val selectedGameVersion: StateFlow<GameVersion>
    fun setSelectedGameVersion(version: GameVersion)

    val useMonospaceScore: StateFlow<Boolean>
    fun setUseMonospaceScore(use: Boolean)

    companion object {
        const val KEY_GAME_VERSION = "KEY_GAME_VERSION"
        const val KEY_USE_MONOSPACE_SCORE = "KEY_USE_MONOSPACE_SCORE"
    }
}

@OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class)
class DefaultLadderSettings : SettingsManager(), LadderSettings {
    override val selectedGameVersion: StateFlow<GameVersion> =
        settings.getStringFlow(KEY_GAME_VERSION, "")
            .map { GameVersion.parse(it) ?: GameVersion.defaultVersion }
            .stateIn(mainScope, SharingStarted.Lazily, GameVersion.defaultVersion)

    override fun setSelectedGameVersion(version: GameVersion) {
        mainScope.launch {
            settings.putString(KEY_GAME_VERSION, version.name)
        }
    }

    override val useMonospaceScore: StateFlow<Boolean> =
        settings.getBooleanFlow(KEY_USE_MONOSPACE_SCORE, false)
            .stateIn(mainScope, SharingStarted.Lazily, false)

    override fun setUseMonospaceScore(use: Boolean) {
        mainScope.launch {
            settings.putBoolean(KEY_USE_MONOSPACE_SCORE, use)
        }
    }
}