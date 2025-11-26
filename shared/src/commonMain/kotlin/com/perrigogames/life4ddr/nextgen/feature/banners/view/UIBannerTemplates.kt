package com.perrigogames.life4ddr.nextgen.feature.banners.view

import com.perrigogames.life4ddr.nextgen.MR
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.color.asColorDesc

object UIBannerTemplates {

    fun success(text: StringDesc) = UIBanner(
        text = text,
        backgroundColor = MR.colors.perfect_transparent.asColorDesc(), // FIXME
        textColor = MR.colors.white.asColorDesc() // FIXME
    )

    fun error(text: StringDesc) = UIBanner(
        text = text,
        backgroundColor = MR.colors.life4.asColorDesc(), // FIXME
    )

    val dummy = UIBanner(
        text = StringDesc.Raw("Testing..."),
        backgroundColor = MR.colors.colorAccent.asColorDesc(),
        textColor = MR.colors.white.asColorDesc()
    )
}
