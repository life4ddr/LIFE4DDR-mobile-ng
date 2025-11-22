package com.perrigogames.life4ddr.nextgen

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

// TODO Settings

actual val platformModule: Module = module {
    single<SqlDriver> { AndroidSqliteDriver(Life4Db.Schema, get(), "Life4Db") }
//    single<Settings> { SharedPreferencesSettings.Factory(get()).create() }
//    single<DataStore<Preferences>> { get<Context>().dataStore }
//    single<FlowSettings> { DataStoreSettings(get()) }
}

//val Context.dataStore by preferencesDataStore("preferences")
