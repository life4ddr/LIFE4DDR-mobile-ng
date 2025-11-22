package com.perrigogames.life4ddr.nextgen.feature.ladder.data

import com.perrigogames.life4ddr.nextgen.data.DifficultyClassSet
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank

// TODO MokoResources
// destructive edits
object RankStrings {
    /** Burn X calories in one day. */
    fun getCalorieCountString(count: Int) = ""

    /**
     * Complete X different DIFF's in a row.
     * Complete a set of DIFF, DIFF, and DIFF.
     */
    fun getSongSetString(clearType: ClearType, difficulties: IntArray) = ""

    /**
     * Earn RANK on any Trial.
     * Earn RANK on X Trials.
     */
    fun getTrialCountString(rank: TrialRank, count: Int) = ""

    /** Earn X MA Points. */
    fun getMAPointString(count: Double) = ""

    // Song Set
    // any Mix or Letter folder / any 3 Mix or Letter folders
    fun folderString(folderCount: Int) = ""

    // the 1st Mix folder
    fun folderString(folderName: String) = ""

    // New Century, Rising Fire Hawk, and Astrogazer
    fun songListString(songs: List<String>) = ""

    // any 3 songs
    fun songCountString(songCount: Int) = ""

    // Score 945,000 on <group> / AAA <group>
    fun scoreString(score: Int, groupString: String) = ""

    // Score an average of 999,500 on <group>
    fun averageScoreString(averageScore: Int, groupString: String) = ""

    // Clear <group>
    fun clearString(clearType: ClearType, useLamp: Boolean, groupString: String) = ""

    fun clearString(clearType: ClearType, useLamp: Boolean) =
        if (useLamp) clearLampString(clearType)
        else clearTypeString(clearType)

    fun clearTypeString(clearType: ClearType) = ""

    fun clearLampString(clearType: ClearType) = ""

    // on ESP and CSP / on DSP, ESP, or CSP
    fun difficultyClassSetModifier(
        groupString: String,
        diffClassSet: DifficultyClassSet,
        playStyle: PlayStyle
    ) = ""

    // (except 5)
    fun exceptionsModifier(groupString: String, exceptions: Int) = ""

    // (except 5, which require 999,500)
    fun steppedExceptionsModifier(
        groupString: String,
        exceptions: Int,
        exceptionScore: Int
    ) = ""

    // (except SongA, SongB, and SongC)
    fun songExceptionsModifier(groupString: String, songExceptions: List<String>) = ""

    // a L15 / an L8
    fun diffNumSingle(diffNum: Int, allowsHigherDiffNum: Boolean) = ""

    // 3 L5s
    fun diffNumCount(count: Int, diffNum: Int, allowsHigherDiffNum: Boolean) = ""

    // all L14s
    fun diffNumAll(diffNum: Int, allowsHigherDiffNum: Boolean) = ""

    fun higherDiffNumSuffix(allowsHigherDiffNum: Boolean): String = when (allowsHigherDiffNum) {
        true -> "+"
        false -> ""
    }
}
