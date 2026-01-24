package com.perrigogames.life4ddr.nextgen.enums

import com.perrigogames.life4ddr.nextgen.MR

fun flareTextResource(level: Int) = when(level) {
    1 -> MR.strings.flare_1
    2 -> MR.strings.flare_2
    3 -> MR.strings.flare_3
    4 -> MR.strings.flare_4
    5 -> MR.strings.flare_5
    6 -> MR.strings.flare_6
    7 -> MR.strings.flare_7
    8 -> MR.strings.flare_8
    9 -> MR.strings.flare_9
    10 -> MR.strings.flare_ex
    else -> null
}

fun flareImageResource(level: Int) = when(level) {
    1 -> MR.images.flare_1
    2 -> MR.images.flare_2
    3 -> MR.images.flare_3
    4 -> MR.images.flare_4
    5 -> MR.images.flare_5
    6 -> MR.images.flare_6
    7 -> MR.images.flare_7
    8 -> MR.images.flare_8
    9 -> MR.images.flare_9
    10 -> MR.images.flare_ex
    else -> null
}

fun flareScoreValue(difficultyNumber: Int, flare: Int): Long {
    val safeFlare = flare.coerceIn(0..10)
    val safeDifficulty = difficultyNumber.coerceIn(1..19)
    return flareScores[safeDifficulty]!![safeFlare].toLong()
}

private val flareScores = mapOf(
    1 to listOf(145, 153, 162, 171, 179, 188, 197, 205, 214, 223, 232),
    2 to listOf(155, 164, 182, 192, 201, 210, 220, 220, 229, 238, 248),
    3 to listOf(170, 180, 190, 200, 210, 221, 231, 241, 251, 261, 272),
    4 to listOf(185, 196, 207, 218, 229, 240, 251, 262, 273, 284, 296),
    5 to listOf(205, 217, 229, 241, 254, 266, 278, 291, 303, 315, 328),
    6 to listOf(230, 243, 257, 271, 285, 299, 312, 326, 340, 354, 368),
    7 to listOf(255, 270, 285, 300, 316, 331, 346, 362, 377, 392, 408),
    8 to listOf(290, 307, 324, 342, 359, 377, 394, 411, 429, 446, 464),
    9 to listOf(335, 355, 375, 395, 415, 435, 455, 475, 495, 515, 536),
    10 to listOf(400, 424, 448, 472, 496, 520, 544, 568, 592, 616, 640),
    11 to listOf(465, 492, 520, 548, 576, 604, 632, 660, 688, 716, 744),
    12 to listOf(510, 540, 571, 601, 632, 663, 693, 724, 754, 785, 816),
    13 to listOf(545, 577, 610, 643, 675, 708, 741, 773, 806, 839, 872),
    14 to listOf(575, 609, 644, 678, 713, 747, 782, 816, 851, 885, 920),
    15 to listOf(600, 636, 672, 708, 744, 780, 816, 852, 888, 924, 960),
    16 to listOf(620, 657, 694, 731, 768, 806, 843, 880, 917, 954, 992),
    17 to listOf(635, 673, 711, 749, 787, 825, 863, 901, 939, 977, 1016),
    18 to listOf(650, 689, 728, 767, 806, 845, 884, 923, 962, 1001, 1040),
    19 to listOf(665, 704, 744, 784, 824, 864, 904, 944, 984, 1024, 1064),
)
