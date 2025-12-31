package com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.banners.enums.BannerLocation
import com.perrigogames.life4ddr.nextgen.feature.banners.manager.BannerManager
import com.perrigogames.life4ddr.nextgen.feature.banners.view.UIBanner
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.GoalListConfig
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.GoalListInput
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.GoalListViewModel
import com.perrigogames.life4ddr.nextgen.feature.profile.data.ProfileHeader
import com.perrigogames.life4ddr.nextgen.feature.profile.data.SocialNetwork
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserRankSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerProfileViewModel(
    private val userRankSettings: UserRankSettings,
    private val infoSettings: UserInfoSettings,
    private val bannerManager: BannerManager,
    private val songResultsManager: SongResultsManager,
) : ViewModel(), KoinComponent {

    private val _playerInfoViewState = MutableStateFlow(PlayerInfoViewState())
    val playerInfoViewState: StateFlow<PlayerInfoViewState> = _playerInfoViewState.asStateFlow()

    private val _headerViewState = MutableStateFlow<ProfileHeader?>(null)
    val headerViewState: StateFlow<ProfileHeader?> = _headerViewState.asStateFlow()

    val goalListViewModel = GoalListViewModel(GoalListConfig(allowHidingCompletedGoals = true))

    private val _events = MutableSharedFlow<PlayerProfileEvent>()
    val events: Flow<PlayerProfileEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                infoSettings.userName,
                infoSettings.rivalCodeDisplay,
                infoSettings.socialNetworks,
                userRankSettings.rank,
                bannerManager.getBannerFlow(BannerLocation.PROFILE)
            ) { userName, rivalCode, socialNetworks, rank, banner ->
                PlayerInfoViewState(userName, rivalCode, socialNetworks, rank, banner)
            }.collect { _playerInfoViewState.value = it }
        }
        viewModelScope.launch {
            songResultsManager.hasResults.collect { hasResults ->
                _headerViewState.value = if (!hasResults) {
                    ProfileHeader.sanbaiReminder
                } else {
                    null
                }
            }
        }
    }

    fun handleInput(input: PlayerProfileInput) = when(input) {
        PlayerProfileInput.SanbaiReminderClicked -> viewModelScope.launch {
            _events.emit(PlayerProfileEvent.NavigateToScores)
        }
        is PlayerProfileInput.GoalList -> goalListViewModel.handleInput(input.input)
        PlayerProfileInput.ChangeRankClicked -> viewModelScope.launch {
            _events.emit(PlayerProfileEvent.NavigateToChangeRank)
        }
    }
}

data class PlayerInfoViewState(
    val username: String = "",
    val rivalCode: String? = null,
    val socialNetworks: Map<SocialNetwork, String> = emptyMap(),
    val rank: LadderRank? = null,
    val banner: UIBanner? = null
)

sealed class PlayerProfileInput {
    data object ChangeRankClicked: PlayerProfileInput()
    data object SanbaiReminderClicked: PlayerProfileInput()
    data class GoalList(val input: GoalListInput): PlayerProfileInput()
}

sealed class PlayerProfileEvent {
    data object NavigateToChangeRank: PlayerProfileEvent()
    data object NavigateToScores: PlayerProfileEvent()
}