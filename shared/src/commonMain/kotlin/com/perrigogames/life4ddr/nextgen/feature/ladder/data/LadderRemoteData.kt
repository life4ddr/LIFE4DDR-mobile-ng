package com.perrigogames.life4ddr.nextgen.feature.ladder.data

import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.RANKS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.base.CachedData
import com.perrigogames.life4ddr.nextgen.api.base.CompositeData
import com.perrigogames.life4ddr.nextgen.api.base.Converter
import com.perrigogames.life4ddr.nextgen.api.base.LocalData
import com.perrigogames.life4ddr.nextgen.api.base.LocalDataReader
import com.perrigogames.life4ddr.nextgen.api.base.RemoteData
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class LadderRemoteData(
    private val json: Json,
    private val githubKtor: GithubDataAPI,
    reader: LocalDataReader,
    converter: LadderRankDataConverter = LadderRankDataConverter(json),
    logger: Logger? = null
): CompositeData<LadderRankData>(
    rawData = LocalData(reader, converter),
    cacheData = CachedData(reader, converter, converter),
    remoteData = object : RemoteData<LadderRankData>(logger) {
        override suspend fun getRemoteResponse() = githubKtor.getLadderRanks()
    },
    logger = logger,
), KoinComponent

class LadderRankDataConverter(
    private val json: Json
): Converter<LadderRankData> {
    override fun create(s: String) = json.decodeFromString(LadderRankData.serializer(), s)
    override fun create(data: LadderRankData) = json.encodeToString(LadderRankData.serializer(), data)
}
