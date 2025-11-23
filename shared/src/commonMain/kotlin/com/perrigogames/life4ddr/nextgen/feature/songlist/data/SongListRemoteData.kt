package com.perrigogames.life4ddr.nextgen.feature.songlist.data

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

class SongListRemoteData: CompositeData<SongListResponse>(), KoinComponent {

    private val json: Json by inject()
    private val reader: LocalDataReader by inject(named(SONGS_FILE_NAME))
    private val sanbaiApi: SanbaiAPI by inject()

    private val converter = SongListConverter()

    override val rawData = LocalData(reader, converter)
    override val cacheData = CachedData(reader, converter, converter)
    override val remoteData = object: RemoteData<SongListResponse>() {
        override val logger = this@SongListRemoteData.logger
        override suspend fun getRemoteResponse() = sanbaiApi.getSongData()
    }

    private inner class SongListConverter: Converter<SongListResponse> {
        override fun create(s: String): SongListResponse =
            json.decodeFromString(SongListResponse.serializer(), s)

        override fun create(data: SongListResponse) =
            json.encodeToString(SongListResponse.serializer(), data)
    }
}
