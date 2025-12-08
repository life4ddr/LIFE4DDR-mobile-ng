package com.perrigogames.life4ddr.nextgen

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module
import java.util.*

// TODO Settings

@OptIn(ExperimentalSettingsApi::class)
actual val platformModule = module {
    single<Properties> { Properties() }
    single<SqlDriver> { JdbcSqliteDriver("jdbc:sqlite:test.db", get(), Life4Db.Schema) }
    single<Settings> { PropertiesSettings(get()) }
}
