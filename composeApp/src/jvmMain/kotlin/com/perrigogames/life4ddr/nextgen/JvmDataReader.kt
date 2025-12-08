package com.perrigogames.life4ddr.nextgen

import com.perrigogames.life4ddr.nextgen.api.base.LocalDataReader
import com.perrigogames.life4ddr.nextgen.api.base.LocalUncachedDataReader
import org.koin.core.component.KoinComponent
import java.io.File
import java.nio.file.Files

open class JvmUncachedDataReader(protected val internalData: String):
    LocalUncachedDataReader, KoinComponent {

    override fun loadInternalString(): String = internalData
}

class JvmDataReader(rawPath: String, private val cachedPath: String): JvmUncachedDataReader(rawPath), LocalDataReader {

    override fun loadCachedString(): String? = File(cachedPath).let {
        if (it.exists()) it.readText() else null
    }

    override fun saveCachedString(data: String): Boolean {
        File(cachedPath).writeText(data)
        return true
    }

    override fun deleteCachedString() = false
}