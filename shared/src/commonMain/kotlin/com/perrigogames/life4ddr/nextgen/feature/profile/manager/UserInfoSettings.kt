package com.perrigogames.life4ddr.nextgen.feature.profile.manager

import com.perrigogames.life4ddr.nextgen.data.GameConstants
import com.perrigogames.life4ddr.nextgen.feature.profile.data.SocialNetwork
import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsManager
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class)
class UserInfoSettings : SettingsManager() {

    val userName: StateFlow<String> = settings.getStringFlow(KEY_INFO_NAME, "")
        .stateIn(mainScope, SharingStarted.Eagerly, "")

    val rivalCode: StateFlow<String> = settings.getStringFlow(KEY_INFO_RIVAL_CODE, "")
        .stateIn(mainScope, SharingStarted.Eagerly, "")

    val rivalCodeDisplay: Flow<String?> = rivalCode.map {
        if (it.length == GameConstants.RIVAL_CODE_LENGTH) {
            "${it.substring(0..3)}-${it.substring(4..7)}"
        } else {
            null
        }
    }

    val socialNetworks: StateFlow<Map<SocialNetwork, String>> =
        settings.getStringFlow(KEY_INFO_SOCIAL_NETWORKS, "")
            .map { settingsString ->
                settingsString.split(SOCIAL_LINE_DELIM)
                    .map { it.split(SOCIAL_ENTRY_DELIM) }
                    .filter { it.size == 2 }
                    .associate { SocialNetwork.parse(it[0]) to it[1] }
            }
            .stateIn(mainScope, SharingStarted.Eagerly, emptyMap())

    fun setUserBasics(
        name: String,
        rivalCode: String,
        socialNetworks: Map<SocialNetwork, String>
    ) {
        setUserName(name)
        setRivalCode(rivalCode)
        setSocialNetworks(socialNetworks)
    }

    fun setUserName(name: String) = mainScope.launch {
        settings.putString(KEY_INFO_NAME, name)
    }

    fun setRivalCode(code: String) = mainScope.launch {
        settings.putString(KEY_INFO_RIVAL_CODE, code)
    }


    fun setSocialNetworks(networks: Map<SocialNetwork, String>) {
        val networksString = networks.toList()
            .joinToString(SOCIAL_LINE_DELIM.toString()) { (k, v) ->
                k.toString() + SOCIAL_ENTRY_DELIM + v
            }

        mainScope.launch {
            settings.putString(KEY_INFO_SOCIAL_NETWORKS, networksString)
        }
    }

    companion object {
        const val SOCIAL_LINE_DELIM = '/'
        const val SOCIAL_ENTRY_DELIM = '"'

        const val KEY_INFO_NAME = "KEY_INFO_NAME"
        const val KEY_INFO_RIVAL_CODE = "KEY_INFO_RIVAL_CODE"
        const val KEY_INFO_SOCIAL_NETWORKS = "KEY_INFO_SOCIAL_NETWORKS"
    }
}