package com.perrigogames.life4ddr.nextgen.view

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.songlist.data.Chart
import com.perrigogames.life4ddr.nextgen.feature.songlist.data.Song
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

sealed class UISongJacket {

    data class WithUrl(val url: String) : UISongJacket()

    data class Placeholder(
        val warningText: StringDesc = MR.strings.no_jacket_found.desc(),
        val chart: Chart,
        val type: PlaceholderType,
    ) : UISongJacket()

    companion object {
        fun fromChart(
            chart: Chart,
            url: String?,
            placeholderType: PlaceholderType
        ): UISongJacket = if (url != null) {
            WithUrl(url)
        } else {
            Placeholder(chart = chart, type = placeholderType)
        }
    }

    enum class PlaceholderType {
        FULL, THUMBNAIL
    }
}
