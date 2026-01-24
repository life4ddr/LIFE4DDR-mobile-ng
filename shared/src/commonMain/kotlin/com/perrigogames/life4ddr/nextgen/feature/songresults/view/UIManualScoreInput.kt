package com.perrigogames.life4ddr.nextgen.feature.songresults.view

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.flareTextResource
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

data class UIManualScoreInput(
    val songTitle: StringDesc,
    val songDifficultyClass: DifficultyClass,
    val songDifficultyNumber: StringDesc,
    val scoreLabel: StringDesc = MR.strings.score.desc(),
    val flareLabel: StringDesc = MR.strings.label_flare.desc(),
    val flareOptions: List<Pair<StringDesc, Int>> =
        (0..10).map { (flareTextResource(it) ?: MR.strings.none).desc() to it },
    val clearTypeLabel: StringDesc = MR.strings.label_clear_type.desc(),
    val clearTypeOptions: List<ClearType> = ClearType.entries,
    val submitLabel: StringDesc = MR.strings.submit.desc(),
)
