package com.perrigogames.life4ddr.nextgen.feature.jackets.db

import app.cash.sqldelight.db.SqlDriver
import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.AppInfo
import com.perrigogames.life4ddr.nextgen.PlatformType
import com.perrigogames.life4ddr.nextgen.db.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

class JacketsDatabaseHelper(
    appInfo: AppInfo,
    sqlDriver: SqlDriver,
    logger: Logger
) : DatabaseHelper(sqlDriver, logger) {

    private val _updates = MutableSharedFlow<Unit>(replay = 1)
    val updates = _updates.asSharedFlow()

    private val queries = dbRef.jacketsQueries

    init {
        if (appInfo.platform != PlatformType.IOS) {
            queries.insertJacket(BASE, "https://3icecream.com/img/banners/f/$TEMPLATE.jpg")
        }
    }

    suspend fun putUrl(id: String, url: String) =
        withContext(Dispatchers.Default) {
            url.baseUrl(id)?.let { baseUrl ->
                queries.insertJacket(BASE, baseUrl)
            } ?: run {
                queries.insertJacket(id, url)
            }
            _updates.emit(Unit)
        }

    fun getUrl(id: String): String? {
        return queries.selectJacket(BASE).executeAsOneOrNull()
            ?.replace(TEMPLATE, id)
            ?: queries.selectJacket(id).executeAsOneOrNull()
    }

    suspend fun clearJackets() = withContext(Dispatchers.Default) {
        queries.clearJackets()
        _updates.emit(Unit)
    }

    private fun String.baseUrl(id: String): String? =
        if (contains(id)) {
            replace(id, TEMPLATE)
        } else null

    companion object {
        const val TEMPLATE = "{SKILL_ID}"
        const val BASE = "BASE"
    }
}