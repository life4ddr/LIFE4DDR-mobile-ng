package com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.data.GameConstants
import com.perrigogames.life4ddr.nextgen.feature.trials.data.Course
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialGoalSet.GoalType.*
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITrialBottomSheet
import com.perrigogames.life4ddr.nextgen.feature.trialsession.data.InProgressTrialSession
import com.perrigogames.life4ddr.nextgen.feature.trialsession.enums.ShortcutType
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import kotlin.math.min

class SongEntryViewModel(
    private val session: InProgressTrialSession,
    private val targetRank: TrialRank,
    private val index: Int,
    shortcut: ShortcutType? = null,
    private val isEdit: Boolean,
): ViewModel(), KoinComponent {

    val passedChecked: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val song = session.trial.songs[index]
    val result get() = session.results[index]!!

    private val _shortcut: MutableStateFlow<ShortcutType?> = MutableStateFlow(shortcut)
    private val _numberMap = MutableStateFlow(mapOf(
        ID_SCORE to result.score,
        ID_EX_SCORE to result.exScore,
        ID_MISSES to result.misses,
        ID_GOODS to result.goods,
        ID_GREATS to result.greats,
        ID_PERFECTS to result.perfects,
        ID_PASSED to if (result.passed) 1 else 0,
    ))
    private val _disabledMap = MutableStateFlow(emptyList<String>())
    private val _viewDataMap = MutableStateFlow(mapOf(
        ID_SCORE to UITrialBottomSheet.Field(
            id = ID_SCORE,
            text = "",
            weight = 2f,
            label = MR.strings.score.desc()
        ),
        ID_EX_SCORE to UITrialBottomSheet.Field(
            id = ID_EX_SCORE,
            text = "",
            label = MR.strings.ex_score.desc()
        ),
        ID_MISSES to UITrialBottomSheet.Field(
            id = ID_MISSES,
            text = "",
            label = MR.strings.misses.desc()
        ),
        ID_GOODS to UITrialBottomSheet.Field(
            id = ID_GOODS,
            text = "",
            label = MR.strings.goods.desc()
        ),
        ID_GREATS to UITrialBottomSheet.Field(
            id = ID_GREATS,
            text = "",
            label = MR.strings.greats.desc()
        ),
        ID_PERFECTS to UITrialBottomSheet.Field(
            id = ID_PERFECTS,
            text = "",
            label = MR.strings.perfects.desc()
        ),
    ))

    private val _state = MutableStateFlow(generateViewState(shortcut))
    val state: StateFlow<UITrialBottomSheet.Details> = _state

    init {
        setShortcutState(shortcut, allowNumberModification = false)
    }

    private fun createField(
        id: String,
        enabled: Boolean = !_disabledMap.value.contains(id),
    ): UITrialBottomSheet.Field {
        val number = _numberMap.value[id]
        return _viewDataMap.value[id]!!.copy(
            text = if (number != null && number != 0) number.toString() else "",
            enabled = enabled,
        )
    }

    private fun createFields(vararg ids: String) = ids.map { createField(it) }

    private fun generateViewState(
        shortcut: ShortcutType?
    ): UITrialBottomSheet.Details {
        val requiredFields = requiredFields(session.trial, targetRank)
        val scoreRequired = requiredFields.contains(ID_SCORE)
        return UITrialBottomSheet.Details(
            imagePath = session.results[index]?.photoUriString.orEmpty(),
            fields = when {
                shortcut == null -> {
                    requiredFields.fold(mutableListOf<MutableList<UITrialBottomSheet.Field>>()) { acc, id ->
                        if (id == NEWLINE) {
                            acc.add(mutableListOf())
                        } else {
                            if (acc.isEmpty()) acc.add(mutableListOf())
                            acc.last().add(createField(id))
                        }
                        acc
                    }
                }
                scoreRequired -> { when (shortcut) {
                    ShortcutType.MFC -> listOf(createFields(ID_SCORE, ID_EX_SCORE))
                    ShortcutType.PFC -> listOf(createFields(ID_SCORE, ID_EX_SCORE, ID_PERFECTS))
                    ShortcutType.GFC -> listOf(
                        createFields(ID_SCORE, ID_EX_SCORE),
                        createFields(ID_PERFECTS, ID_GREATS),
                    )
                } }
                else -> { when (shortcut) {
                    ShortcutType.MFC -> listOf(createFields(ID_EX_SCORE))
                    ShortcutType.PFC -> listOf(createFields(ID_EX_SCORE, ID_PERFECTS))
                    ShortcutType.GFC -> listOf(
                        createFields(ID_EX_SCORE),
                        createFields(ID_PERFECTS, ID_GREATS)
                    )
                } }
            },
            isEdit = isEdit,
            shortcuts = listOf(
                UITrialBottomSheet.Shortcut(
                    MR.strings.clear_mfc.desc(),
                    TrialSessionInput.UseShortcut(ShortcutType.MFC)
                ),
                UITrialBottomSheet.Shortcut(
                    MR.strings.clear_pfc.desc(),
                    TrialSessionInput.UseShortcut(ShortcutType.PFC)
                ),
                UITrialBottomSheet.Shortcut(
                    MR.strings.clear_gfc.desc(),
                    TrialSessionInput.UseShortcut(ShortcutType.GFC)
                ),
                UITrialBottomSheet.Shortcut(
                    MR.strings.none.desc(),
                    TrialSessionInput.UseShortcut(null)
                ),
            ),
            shortcutColor = when (shortcut) {
                ShortcutType.MFC -> MR.colors.marvelous
                ShortcutType.PFC -> MR.colors.perfect
                ShortcutType.GFC -> MR.colors.great
                else -> null
            }
        )
    }

    fun setShortcutState(
        shortcut: ShortcutType?,
        allowNumberModification: Boolean = true,
    ) {
        _shortcut.value = shortcut
        if (allowNumberModification) {
            processShortcutNumberChanges(shortcut)
        }
        processShortcutEnabledChanges(shortcut)
        _state.value = generateViewState(shortcut)
    }

    private fun processShortcutNumberChanges(shortcut: ShortcutType?) {
        _numberMap.update {
            when (shortcut) {
                ShortcutType.MFC,
                ShortcutType.PFC -> {
                    mapOf(
                        ID_SCORE to GameConstants.MAX_SCORE,
                        ID_EX_SCORE to song.ex,
                        ID_MISSES to 0,
                        ID_GOODS to 0,
                        ID_GREATS to 0,
                        ID_PERFECTS to 0.takeIf { shortcut == ShortcutType.MFC },
                    )
                }

                ShortcutType.GFC -> {
                    mapOf(
                        ID_SCORE to null,
                        ID_EX_SCORE to song.ex,
                        ID_MISSES to 0,
                        ID_GOODS to 0,
                        ID_GREATS to null,
                        ID_PERFECTS to null,
                    )
                }

                null -> {
                    mapOf(
                        ID_SCORE to null,
                        ID_EX_SCORE to null,
                        ID_MISSES to null,
                        ID_GOODS to null,
                        ID_GREATS to null,
                        ID_PERFECTS to null,
                    )
                }
            }
        }
    }

    private fun processShortcutEnabledChanges(shortcut: ShortcutType?) {
        when (shortcut) {
            ShortcutType.MFC -> _disabledMap.update {
                listOf(ID_SCORE, ID_EX_SCORE, ID_MISSES, ID_GOODS, ID_GREATS, ID_PERFECTS)
            }
            ShortcutType.PFC -> _disabledMap.update {
                listOf(ID_SCORE, ID_EX_SCORE, ID_MISSES, ID_GOODS, ID_GREATS)
            }
            ShortcutType.GFC -> _disabledMap.update {
                listOf(ID_MISSES, ID_GOODS, ID_EX_SCORE)
            }
            else -> _disabledMap.update { emptyList() }
        }
    }

    fun changeText(id: String, text: String) {
        val number = text.toIntOrNull()
        when (_shortcut.value) {
            ShortcutType.MFC -> {} // No fields are enabled for MFC
            ShortcutType.PFC -> { // Perfects enabled, calculate score and EX
                val perfects = (number ?: 0).takeIf {id == ID_PERFECTS } ?: _numberMap.value[ID_PERFECTS] ?: 0
                _numberMap.update { map ->
                    map.toMutableMap().apply {
                        put(ID_SCORE, GameConstants.MAX_SCORE - (perfects * 10))
                        put(ID_EX_SCORE, song.ex - perfects)
                        put(id, number)
                    }
                }
            }
            ShortcutType.GFC -> {
                val perfects = (number ?: 0).takeIf {id == ID_PERFECTS } ?: _numberMap.value[ID_PERFECTS] ?: 0
                val greats = (number ?: 0).takeIf {id == ID_GREATS } ?: _numberMap.value[ID_GREATS] ?: 0
                _numberMap.update { map ->
                    map.toMutableMap().apply {
                        put(ID_EX_SCORE, song.ex - (perfects + (greats * 2)))
                        put(id, number)
                    }
                }
            }
            else -> _numberMap.update { map ->
                map + (id to number)
            }
        }
        _state.value = generateViewState(_shortcut.value)
    }

    fun commitChanges(): InProgressTrialSession {
        val result = result.copy(
            score = _numberMap.value[ID_SCORE],
            exScore = _numberMap.value[ID_EX_SCORE]?.let {
                min(it, session.trial.songs[index].ex)
            },
            misses = _numberMap.value[ID_MISSES],
            goods = _numberMap.value[ID_GOODS],
            greats = _numberMap.value[ID_GREATS],
            perfects = _numberMap.value[ID_PERFECTS],
            passed = passedChecked.value,
            shortcut = _shortcut.value
        )
        return session.copy(
            results = session.results.copyOf().also {
                it[index] = result
            }
        )
    }

    private fun requiredFields(trial: Course.Trial, targetRank: TrialRank): List<String> {
        val goalTypes = trial.goalSet(targetRank)
            ?.goalTypes
            ?.toMutableList()
            ?: return emptyList()

        val out = mutableListOf<String>()
        if (goalTypes.contains(SCORE)) {
            out += ID_SCORE
        }
        out += ID_EX_SCORE
        if (goalTypes.contains(BAD_JUDGEMENT)) {
            out += ID_GREATS
            out += ID_GOODS
        }
        if (goalTypes.contains(MISS) || goalTypes.contains(BAD_JUDGEMENT)) {
            out += ID_MISSES
            val newlineIndex = out.indexOfFirst { it == ID_EX_SCORE } + 1
            out.add(newlineIndex, NEWLINE)
        }
        return out.toList()
    }

    companion object {
        const val ID_SCORE = "score"
        const val ID_EX_SCORE = "ex_score"
        const val ID_MISSES = "misses"
        const val ID_GOODS = "goods"
        const val ID_GREATS = "greats"
        const val ID_PERFECTS = "perfects"
        const val ID_PASSED = "passed"
        const val NEWLINE = "newline"
    }
}
