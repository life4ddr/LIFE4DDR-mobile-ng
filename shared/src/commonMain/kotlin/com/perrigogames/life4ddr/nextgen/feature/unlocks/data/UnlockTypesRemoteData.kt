package com.perrigogames.life4ddr.nextgen.feature.unlocks.data

import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI
import com.perrigogames.life4ddr.nextgen.api.base.CachedData
import com.perrigogames.life4ddr.nextgen.api.base.CompositeData
import com.perrigogames.life4ddr.nextgen.api.base.Converter
import com.perrigogames.life4ddr.nextgen.api.base.LocalData
import com.perrigogames.life4ddr.nextgen.api.base.LocalDataReader
import com.perrigogames.life4ddr.nextgen.api.base.RemoteData
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent

class UnlockTypesRemoteData(
    private val json: Json,
    private val githubKtor: GithubDataAPI,
    reader: LocalDataReader,
    converter: UnlockTypesConverter = UnlockTypesConverter(json),
    logger: Logger? = null
) : CompositeData<UnlockTypes>(
    rawData = LocalData(reader, converter),
    cacheData = CachedData(reader, converter, converter),
    remoteData = object : RemoteData<UnlockTypes>(logger) {
        override suspend fun getRemoteResponse() = githubKtor.getUnlockTypes()
    },
    logger = logger,
), KoinComponent

class UnlockTypesConverter(
    private val json: Json
) : Converter<UnlockTypes> {
    override fun create(s: String): UnlockTypes = json.decodeFromString(UnlockTypes.serializer(), s)
    override fun create(data: UnlockTypes): String = json.encodeToString(UnlockTypes.serializer(), data)
}