package com.perrigogames.life4ddr.nextgen.compose

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.perrigogames.life4ddr.nextgen.MR
import dev.icerock.moko.resources.compose.fontFamilyResource

object FontSizes {
    val TINY = 10.sp
    val SMALL = 12.sp
    val MEDIUM = 14.sp
    val LARGE = 16.sp
    val HUGE = 22.sp
    val GIANT = 28.sp
}

@Composable
fun LIFE4Typography() = Typography(
    headlineLarge = Fonts.Headline.Large(),
    headlineMedium = Fonts.Headline.Medium(),
    titleLarge = Fonts.Title.Large(),
    titleMedium = Fonts.Title.Medium(),
    bodyLarge = Fonts.Body.Large(),
    bodyMedium = Fonts.Body.Medium(),
    labelLarge = Fonts.Label.Large(),
    labelMedium = Fonts.Label.Medium(),
)

object Fonts {
    object Headline {
        @Composable
        fun Large() = TextStyle(
            fontFamily = fontFamilyResource(MR.fonts.avenirnext_demibold),
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        )

        @Composable
        fun Medium() = TextStyle(
            fontFamily = fontFamilyResource(MR.fonts.avenirnext_demibold),
            fontWeight = FontWeight.SemiBold,
            fontSize = FontSizes.GIANT,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        )

        @Composable
        fun Small() = TextStyle(
            fontFamily = fontFamilyResource(MR.fonts.avenirnext_demibold),
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        )
    }

    object Title {
        @Composable
        fun Large() = TextStyle(
            fontFamily = fontFamilyResource(MR.fonts.avenirnext_demibold),
            fontWeight = FontWeight.SemiBold,
            fontSize = FontSizes.HUGE,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        )

        @Composable
        fun Medium() = TextStyle(
            fontFamily = fontFamilyResource(MR.fonts.avenirnext_demibold),
            fontWeight = FontWeight.SemiBold,
            fontSize = FontSizes.LARGE,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        )

        @Composable
        fun Small() = TextStyle(
            fontFamily = fontFamilyResource(MR.fonts.avenirnext_bold),
            fontWeight = FontWeight.Bold,
            fontSize = FontSizes.MEDIUM,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        )
    }

    object Body {
        @Composable
        fun Large() = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = FontSizes.LARGE,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        )

        @Composable
        fun Medium() = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = FontSizes.MEDIUM,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        )

        @Composable
        fun Small() = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = FontSizes.SMALL,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        )
    }

    object Label {
        @Composable
        fun Large() = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = FontSizes.MEDIUM,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        )

        @Composable
        fun Medium() = TextStyle(
            fontFamily = fontFamilyResource(MR.fonts.avenirnext_heavy),
            fontWeight = FontWeight.Black,
            fontSize = FontSizes.SMALL,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )

        @Composable
        fun Small() = TextStyle(
            fontFamily = fontFamilyResource(MR.fonts.avenirnext_heavy),
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    }
}
