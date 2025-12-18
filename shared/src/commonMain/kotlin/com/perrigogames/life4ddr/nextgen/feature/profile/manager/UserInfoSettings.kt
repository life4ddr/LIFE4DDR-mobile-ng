package com.perrigogames.life4ddr.nextgen.feature.profile.manager

import com.perrigogames.life4ddr.nextgen.data.GameConstants
import com.perrigogames.life4ddr.nextgen.feature.profile.data.SocialNetwork
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings.Companion.KEY_INFO_NAME
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings.Companion.KEY_INFO_RIVAL_CODE
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings.Companion.KEY_INFO_SOCIAL_NETWORKS
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings.Companion.SOCIAL_ENTRY_DELIM
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings.Companion.SOCIAL_LINE_DELIM
import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsManager
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface UserInfoSettings {

    val userName: StateFlow<String>
    val rivalCode: StateFlow<String>
    val rivalCodeDisplay: Flow<String?>
    val socialNetworks: StateFlow<Map<SocialNetwork, String>>

    fun setUserBasics(
        name: String,
        rivalCode: String,
        socialNetworks: Map<SocialNetwork, String>
    )
    fun setUserName(name: String)
    fun setRivalCode(code: String)
    fun setSocialNetworks(networks: Map<SocialNetwork, String>)

    companion object {
        const val SOCIAL_LINE_DELIM = '/'
        const val SOCIAL_ENTRY_DELIM = '"'

        const val KEY_INFO_NAME = "KEY_INFO_NAME"
        const val KEY_INFO_RIVAL_CODE = "KEY_INFO_RIVAL_CODE"
        const val KEY_INFO_SOCIAL_NETWORKS = "KEY_INFO_SOCIAL_NETWORKS"
    }
}

@OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class)
class DefaultUserInfoSettings : SettingsManager(), UserInfoSettings {

    override val userName: StateFlow<String> = settings.getStringFlow(KEY_INFO_NAME, "")
        .stateIn(mainScope, SharingStarted.Eagerly, "")

    override val rivalCode: StateFlow<String> = settings.getStringFlow(KEY_INFO_RIVAL_CODE, "")
        .stateIn(mainScope, SharingStarted.Eagerly, "")

    override val rivalCodeDisplay: Flow<String?> = rivalCode.map {
        when (it.length) {
            GameConstants.RIVAL_CODE_LENGTH + 1 -> it
            GameConstants.RIVAL_CODE_LENGTH -> {
                "${it.substring(0..3)}-${it.substring(4..7)}"
            }
            else -> null
        }
    }

    override val socialNetworks: StateFlow<Map<SocialNetwork, String>> =
        settings.getStringFlow(KEY_INFO_SOCIAL_NETWORKS, "")
            .map { settingsString ->
                settingsString.split(SOCIAL_LINE_DELIM)
                    .map { it.split(SOCIAL_ENTRY_DELIM) }
                    .filter { it.size == 2 }
                    .associate { SocialNetwork.parse(it[0]) to it[1] }
            }
            .stateIn(mainScope, SharingStarted.Eagerly, emptyMap())

    override fun setUserBasics(
        name: String,
        rivalCode: String,
        socialNetworks: Map<SocialNetwork, String>
    ) {
        setUserName(name)
        setRivalCode(rivalCode)
        setSocialNetworks(socialNetworks)
    }

    override fun setUserName(name: String) {
        mainScope.launch {
            settings.putString(KEY_INFO_NAME, name)
        }
    }

    override fun setRivalCode(code: String) {
        mainScope.launch {
            settings.putString(KEY_INFO_RIVAL_CODE, code)
        }
    }

    override fun setSocialNetworks(networks: Map<SocialNetwork, String>) {
        val networksString = networks.toList()
            .joinToString(SOCIAL_LINE_DELIM.toString()) { (k, v) ->
                k.toString() + SOCIAL_ENTRY_DELIM + v
            }

        mainScope.launch {
            settings.putString(KEY_INFO_SOCIAL_NETWORKS, networksString)
        }
    }
}