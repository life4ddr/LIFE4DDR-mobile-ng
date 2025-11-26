
package com.perrigogames.life4ddr.nextgen

import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank

val LadderRank.drawableRes get() = when(this) {
    LadderRank.COPPER1 -> MR.images.copper_1
    LadderRank.COPPER2 -> MR.images.copper_2
    LadderRank.COPPER3 -> MR.images.copper_3
    LadderRank.COPPER4 -> MR.images.copper_4
    LadderRank.COPPER5 -> MR.images.copper_5
    LadderRank.BRONZE1 -> MR.images.bronze_1
    LadderRank.BRONZE2 -> MR.images.bronze_2
    LadderRank.BRONZE3 -> MR.images.bronze_3
    LadderRank.BRONZE4 -> MR.images.bronze_4
    LadderRank.BRONZE5 -> MR.images.bronze_5
    LadderRank.SILVER1 -> MR.images.silver_1
    LadderRank.SILVER2 -> MR.images.silver_2
    LadderRank.SILVER3 -> MR.images.silver_3
    LadderRank.SILVER4 -> MR.images.silver_4
    LadderRank.SILVER5 -> MR.images.silver_5
    LadderRank.GOLD1 -> MR.images.gold_1
    LadderRank.GOLD2 -> MR.images.gold_2
    LadderRank.GOLD3 -> MR.images.gold_3
    LadderRank.GOLD4 -> MR.images.gold_4
    LadderRank.GOLD5 -> MR.images.gold_5
    LadderRank.PLATINUM1 -> MR.images.platinum_1
    LadderRank.PLATINUM2 -> MR.images.platinum_2
    LadderRank.PLATINUM3 -> MR.images.platinum_3
    LadderRank.PLATINUM4 -> MR.images.platinum_4
    LadderRank.PLATINUM5 -> MR.images.platinum_5
    LadderRank.DIAMOND1 -> MR.images.diamond_1
    LadderRank.DIAMOND2 -> MR.images.diamond_2
    LadderRank.DIAMOND3 -> MR.images.diamond_3
    LadderRank.DIAMOND4 -> MR.images.diamond_4
    LadderRank.DIAMOND5 -> MR.images.diamond_5
    LadderRank.COBALT1 -> MR.images.cobalt_1
    LadderRank.COBALT2 -> MR.images.cobalt_2
    LadderRank.COBALT3 -> MR.images.cobalt_3
    LadderRank.COBALT4 -> MR.images.cobalt_4
    LadderRank.COBALT5 -> MR.images.cobalt_5
    LadderRank.PEARL1 -> MR.images.pearl_1
    LadderRank.PEARL2 -> MR.images.pearl_2
    LadderRank.PEARL3 -> MR.images.pearl_3
    LadderRank.PEARL4 -> MR.images.pearl_4
    LadderRank.PEARL5 -> MR.images.pearl_5
    LadderRank.AMETHYST1 -> MR.images.amethyst_1
    LadderRank.AMETHYST2 -> MR.images.amethyst_2
    LadderRank.AMETHYST3 -> MR.images.amethyst_3
    LadderRank.AMETHYST4 -> MR.images.amethyst_4
    LadderRank.AMETHYST5 -> MR.images.amethyst_5
    LadderRank.EMERALD1 -> MR.images.emerald_1
    LadderRank.EMERALD2 -> MR.images.emerald_2
    LadderRank.EMERALD3 -> MR.images.emerald_3
    LadderRank.EMERALD4 -> MR.images.emerald_4
    LadderRank.EMERALD5 -> MR.images.emerald_5
    LadderRank.ONYX1 -> MR.images.onyx_1
    LadderRank.ONYX2 -> MR.images.onyx_2
    LadderRank.ONYX3 -> MR.images.onyx_3
    LadderRank.ONYX4 -> MR.images.onyx_4
    LadderRank.ONYX5 -> MR.images.onyx_5
}

val TrialRank.drawableRes get() = when(this) {
    TrialRank.COPPER -> MR.images.copper_3
    TrialRank.BRONZE -> MR.images.bronze_3
    TrialRank.SILVER -> MR.images.silver_3
    TrialRank.GOLD -> MR.images.gold_3
    TrialRank.PLATINUM -> MR.images.platinum_3
    TrialRank.DIAMOND -> MR.images.diamond_3
    TrialRank.COBALT -> MR.images.cobalt_3
    TrialRank.PEARL -> MR.images.pearl_3
    TrialRank.AMETHYST -> MR.images.amethyst_3
    TrialRank.EMERALD -> MR.images.emerald_3
    TrialRank.ONYX -> MR.images.onyx_3
}
