package com.perrigogames.life4ddr.nextgen.feature.songlist.data

import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.SONGS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPI
import com.perrigogames.life4ddr.nextgen.api.base.CachedData
import com.perrigogames.life4ddr.nextgen.api.base.CompositeData
import com.perrigogames.life4ddr.nextgen.api.base.Converter
import com.perrigogames.life4ddr.nextgen.api.base.LocalData
import com.perrigogames.life4ddr.nextgen.api.base.LocalDataReader
import com.perrigogames.life4ddr.nextgen.api.base.RemoteData
import com.perrigogames.life4ddr.nextgen.feature.sanbai.data.SongListResponse
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlin.getValue

class SongListRemoteData(
    private val json: Json,
    private val sanbaiApi: SanbaiAPI,
    reader: LocalDataReader,
    converter: SongListConverter = SongListConverter(json),
    logger: Logger? = null
): CompositeData<SongListResponse>(
    rawData = LocalData(reader, converter),
    cacheData = CachedData(reader, converter, converter),
    remoteData = object: RemoteData<SongListResponse>(logger) {
        override suspend fun getRemoteResponse() = sanbaiApi.getSongData()
    },
    logger = logger
), KoinComponent

class SongListConverter(
    private val json: Json
): Converter<SongListResponse> {
    override fun create(s: String): SongListResponse = json.decodeFromString(SongListResponse.serializer(), s)
    override fun create(data: SongListResponse) = json.encodeToString(SongListResponse.serializer(), data)
}
