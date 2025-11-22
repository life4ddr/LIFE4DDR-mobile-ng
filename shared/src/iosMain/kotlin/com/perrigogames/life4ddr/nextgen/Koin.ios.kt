package com.perrigogames.life4ddr.nextgen

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

// TODO Settings

actual val platformModule = module {
    single<SqlDriver> { NativeSqliteDriver(Life4Db.Schema, "Life4Db") }
}

fun makeIosExtraModule(
    defaults: NSUserDefaults
) = module {
//    val settings = NSUserDefaultsSettings(defaults)
//    single<Settings> { settings }
//    single<FlowSettings> { settings.toFlowSettings() }
}