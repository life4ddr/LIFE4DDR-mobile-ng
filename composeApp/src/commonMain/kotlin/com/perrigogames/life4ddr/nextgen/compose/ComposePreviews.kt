package com.perrigogames.life4ddr.nextgen.compose

import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

// region Data Providers

class LadderRankParameterProvider : PreviewParameterProvider<LadderRank> {
    override val values = LadderRank.entries.asSequence()
}

class LadderRankLevel1ParameterProvider : PreviewParameterProvider<LadderRank> {
    override val values = LadderRank.entries.filter { it.classPosition == 1 }.asSequence()
}

class LadderRankLevel2ParameterProvider : PreviewParameterProvider<LadderRank> {
    override val values = LadderRank.entries.filter { it.classPosition == 2 }.asSequence()
}

class LadderRankLevel3ParameterProvider : PreviewParameterProvider<LadderRank> {
    override val values = LadderRank.entries.filter { it.classPosition == 3 }.asSequence()
}

class LadderRankLevel4ParameterProvider : PreviewParameterProvider<LadderRank> {
    override val values = LadderRank.entries.filter { it.classPosition == 4 }.asSequence()
}

class LadderRankLevel5ParameterProvider : PreviewParameterProvider<LadderRank> {
    override val values = LadderRank.entries.filter { it.classPosition == 5 }.asSequence()
}

// endregion
