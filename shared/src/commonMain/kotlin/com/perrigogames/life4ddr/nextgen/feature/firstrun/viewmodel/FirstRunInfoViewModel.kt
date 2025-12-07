package com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel

import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.FirstRunSettings
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState.*
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunError.UsernameError
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.*
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.PathStep.*
import com.perrigogames.life4ddr.nextgen.feature.profile.data.SocialNetwork
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings
import com.perrigogames.life4ddr.nextgen.injectLogger
import dev.icerock.moko.mvvm.flow.CFlow
import dev.icerock.moko.mvvm.flow.cFlow
import dev.icerock.moko.mvvm.flow.cMutableStateFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KClass

class FirstRunInfoViewModel : ViewModel(), KoinComponent {

    private val infoSettings: UserInfoSettings by inject()
    private val firstRunSettings: FirstRunSettings by inject()
    private val logger by injectLogger(this::class.simpleName ?: "FirstRunInfoViewModel")

    val username = MutableStateFlow("").cMutableStateFlow()
    val password = MutableStateFlow("").cMutableStateFlow()
    val rivalCode = MutableStateFlow("").cMutableStateFlow()
    val socialNetworks = MutableStateFlow<MutableMap<SocialNetwork, String>>(mutableMapOf()).cMutableStateFlow()

    private val _stateStack = MutableStateFlow<List<FirstRunStep>>(listOf(Landing)).cMutableStateFlow()
    val state: CFlow<FirstRunStep> = _stateStack.map { it.last() }.cFlow()

    private val _events = MutableSharedFlow<FirstRunEvent>()
    val events: SharedFlow<FirstRunEvent> = _events.asSharedFlow()

    private val currentStep: FirstRunStep get() = _stateStack.value.last()
    private val currentPath: FirstRunPath? get() = (currentStep as? FirstRunStep.PathStep)?.path

    private val _errors = MutableStateFlow<List<FirstRunError>>(emptyList()).cMutableStateFlow()
    val errors: CFlow<List<FirstRunError>> = _errors.cFlow()

    inline fun <reified T : FirstRunError> errorOfType() : Flow<T?> =
        errors.map { errors -> errors.firstOrNull { it is T } as? T }

    init {
        viewModelScope.launch {
            infoSettings.userName.collect { username.emit(it) }
        }
        viewModelScope.launch {
            infoSettings.rivalCode.collect { rivalCode.emit(it) }
        }
        viewModelScope.launch {
            infoSettings.socialNetworks.collect { socialNetworks.emit(it.toMutableMap()) }
        }
    }

    fun handleInput(input: FirstRunInput) {
        when(input) {
            FirstRunInput.NavigateBack -> {
                if (!navigateBack()) {
                    viewModelScope.launch {
                        _events.emit(FirstRunEvent.Close)
                    }
                }
            }
            FirstRunInput.NavigateNext -> navigateNext()
            is FirstRunInput.NewUserSelected -> newUserSelected(input.isNewUser)
            is FirstRunInput.RankMathodSelected -> rankMethodSelected(input.method)
            is FirstRunInput.UsernameUpdated -> username.value = input.name
            is FirstRunInput.RivalCodeUpdated -> rivalCode.value = input.rivalCode
        }
    }

    fun newUserSelected(isNewUser: Boolean) {
        require(currentPath == null) { "Called newUserSelected when path is already set to $currentPath" }
        val path = when (isNewUser) {
            true -> FirstRunPath.NEW_USER_LOCAL
            false -> FirstRunPath.EXISTING_USER_LOCAL
        }
        _stateStack.value += createStateClass(path, clazz = path.steps[0])
    }

    private fun <T : FirstRunStep> createStateClass(
        path: FirstRunPath = currentPath!!,
        rankMethod: InitState? = null,
        clazz: KClass<T>,
    ) : FirstRunStep {
        rankMethod?.let { firstRunSettings.setInitState(it) }
        return when (clazz) {
            Username::class -> Username(path)
            Password::class -> Password(path)
            UsernamePassword::class -> UsernamePassword(path)
            RivalCode::class -> RivalCode(path)
            SocialHandles::class -> SocialHandles(path)
            InitialRankSelection::class -> InitialRankSelection(path)
            Completed::class -> Completed(path, rankMethod!!)
            else -> error("Invalid class ${clazz.simpleName}")
        }
    }

    fun rankMethodSelected(method: InitState) {
        infoSettings.setUserBasics(
            name = username.value,
            rivalCode = rivalCode.value,
            socialNetworks = socialNetworks.value,
        )
        firstRunSettings.setInitState(DONE)
        _stateStack.value += createStateClass(rankMethod = method, clazz = nextStep)
    }

    fun navigateNext() {
        when (currentStep) {
            is Username -> {
                if (username.value.isEmpty()) {
                    emitError(UsernameError())
                    return
                }
            }
            is Password -> {
                // TODO validate password
            }
            is UsernamePassword -> {
                // TODO make network call and handle result
                return
            }
            is RivalCode -> {
                val length = rivalCode.value.length
                if (length != 0 && length != 8) {
                    emitError(FirstRunError.RivalCodeError())
                    return
                }
            }
            else -> {}
        }
        clearError()
        appendState(createStateClass(clazz = nextStep))
        logger.d { "${_stateStack.value.size} / ${_stateStack.value.last()}" }
    }

    private fun appendState(step: FirstRunStep) {
        _stateStack.value += step
    }

    private fun emitError(vararg errors: FirstRunError) {
        _errors.value = listOf(*errors)
    }

    private fun clearError() {
        if (_errors.value.isNotEmpty()) {
            _errors.value = emptyList()
        }
    }

    private val nextStep: KClass<out FirstRunStep>
        get() {
            val path = currentPath
            require(path != null) { "Must select a path to advance the step" }
            val currentIndex = path.steps.indexOfFirst { it == currentStep::class }
            return path.steps[currentIndex + 1]
        }

    fun navigateBack(): Boolean {
        if (_stateStack.value.size == 1) {
            return false
        }
        val popped = _stateStack.value.last()
        _stateStack.value -= popped
        clearError()
        return true
    }
}
