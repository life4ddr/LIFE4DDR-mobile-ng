package com.perrigogames.life4ddr.nextgen

import com.perrigogames.life4ddr.nextgen.api.base.LocalDataReader
import com.perrigogames.life4ddr.nextgen.api.base.LocalUncachedDataReader
import org.koin.core.component.KoinComponent

open class JvmUncachedDataReader(protected val rawPath: String):
    LocalUncachedDataReader, KoinComponent {

    override fun loadInternalString(): String = "" // FIXME DataReader
}

class JvmDataReader(rawPath: String, private val cachedPath: String): JvmUncachedDataReader(rawPath), LocalDataReader {

    override fun loadCachedString(): String? = null // FIXME DataReader

    override fun saveCachedString(data: String) = false // FIXME DataReader

    override fun deleteCachedString() = false // FIXME DataReader
}