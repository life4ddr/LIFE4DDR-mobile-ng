package com.perrigogames.life4ddr.nextgen.compose

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

// region Annotations

@Preview(
    name = "light mode",
    group = "light dark modes",
    uiMode = UI_MODE_NIGHT_NO,
)
@Preview(
    name = "dark mode",
    group = "light dark modes",
    uiMode = UI_MODE_NIGHT_YES,
)
annotation class LightDarkModePreviews

@Preview(
    name = "light mode",
    group = "light dark modes",
    uiMode = UI_MODE_NIGHT_NO,
    showSystemUi = true,
)
@Preview(
    name = "dark mode",
    group = "light dark modes",
    uiMode = UI_MODE_NIGHT_YES,
    showSystemUi = true,
)
annotation class LightDarkModeSystemPreviews

// endregion
