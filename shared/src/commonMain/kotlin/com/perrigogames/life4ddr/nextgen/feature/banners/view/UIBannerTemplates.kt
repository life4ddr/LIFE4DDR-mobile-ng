package com.perrigogames.life4ddr.nextgen.feature.banners.view

import com.perrigogames.life4ddr.nextgen.MR
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.color.asColorDesc

object UIBannerTemplates {

    fun success(text: StringDesc) = UIBanner(
        text = text,
        backgroundColor = MR.colors.perfect_transparent, // FIXME
        textColor = MR.colors.white // FIXME
    )

    fun error(text: StringDesc) = UIBanner(
        text = text,
        backgroundColor = MR.colors.life4, // FIXME
    )

    val dummy = UIBanner(
        text = StringDesc.Raw("Testing..."),
        backgroundColor = MR.colors.colorAccent,
        textColor = MR.colors.white
    )
}
