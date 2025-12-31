package com.perrigogames.life4ddr.nextgen

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.perrigogames.life4ddr.nextgen.util.ExternalActions
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val platformModule = module {
    single<SqlDriver> { NativeSqliteDriver(Life4Db.Schema, "Life4Db") }
    single<ExternalActions> { IosExternalActions() }
}

@OptIn(ExperimentalSettingsApi::class)
fun makeIosExtraModule(
    defaults: NSUserDefaults
) = module {
    val settings = NSUserDefaultsSettings(defaults)
    single<Settings> { settings }
    single<FlowSettings> { settings.toFlowSettings() }
}
