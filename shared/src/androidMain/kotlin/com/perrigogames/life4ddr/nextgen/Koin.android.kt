package com.perrigogames.life4ddr.nextgen

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import org.koin.core.module.Module
import org.koin.dsl.module

@OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class)
actual val platformModule: Module = module {
    single<SqlDriver> { AndroidSqliteDriver(Life4Db.Schema, get(), "Life4Db") }
    single<Settings> { SharedPreferencesSettings.Factory(get()).create() }
    single<DataStore<Preferences>> { get<Context>().dataStore }
}

val Context.dataStore by preferencesDataStore("preferences")
