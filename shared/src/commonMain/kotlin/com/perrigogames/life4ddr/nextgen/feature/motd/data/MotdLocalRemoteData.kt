package com.perrigogames.life4ddr.nextgen.feature.motd.data

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
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlin.getValue

class MotdLocalRemoteData(
    private val json: Json,
    private val githubKtor: GithubDataAPI,
    reader: LocalDataReader,
    converter: MessageOfTheDayConverter = MessageOfTheDayConverter(json),
    logger: Logger? = null
): CompositeData<MessageOfTheDay>(
    rawData = LocalData(reader, converter),
    cacheData = CachedData(reader, converter, converter),
    remoteData = object: RemoteData<MessageOfTheDay>(logger) {
        override suspend fun getRemoteResponse() = githubKtor.getMotd()
    },
    logger = logger
), KoinComponent

class MessageOfTheDayConverter(
    private val json: Json
): Converter<MessageOfTheDay> {
    override fun create(s: String) = json.decodeFromString(MessageOfTheDay.serializer(), s)
    override fun create(data: MessageOfTheDay) = json.encodeToString(MessageOfTheDay.serializer(), data)
}