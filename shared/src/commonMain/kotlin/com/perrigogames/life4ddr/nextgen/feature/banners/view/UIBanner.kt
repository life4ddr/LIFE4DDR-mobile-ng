package com.perrigogames.life4ddr.nextgen.feature.banners.view

import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.desc.StringDesc

/**
 * Represents a banner UI element that can display a text with an optional color.
 *
 * @property text The textual content of the banner.
 * @property backgroundColor The background color of the banner. If null, use an appropriate
 *  native adaptable color.
 * @property textColor The color of the banner text. If null, use an appropriate native
 *  adaptable color.
 */
data class UIBanner(
    val text: StringDesc,
    val backgroundColor: ColorResource? = null,
    val textColor: ColorResource? = null,
)
