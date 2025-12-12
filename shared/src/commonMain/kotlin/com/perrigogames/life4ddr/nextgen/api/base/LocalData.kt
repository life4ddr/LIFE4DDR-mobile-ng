package com.perrigogames.life4ddr.nextgen.api.base

/**
 * Interface for retrieving data from the application's resources.
 */
interface LocalUncachedDataReader {

    /**
     * Loads a raw version of the data from the system's resources.
     * This is not optional and serves as the default data set.
     */
    fun loadInternalString(): String
}

/**
 * Interface for retrieving and committing data to/from a cached source,
 * usually elsewhere in local storage.
 */
interface LocalDataReader: LocalUncachedDataReader, CachingDataSource<String> {

    /**
     * Loads the cached version of the data from internal storage, if it exists.
     */
    fun loadCachedString(): String?
}

class LocalData<T: Any>(
    private val localReader: LocalUncachedDataReader,
    private val stringToData: StringToData<T>,
): InstantDataSource<T> {

    override val data: T
        get() = stringToData.create(localReader.loadInternalString())
}

class CachedData<T: Any>(
    private val localReader: LocalDataReader,
    private val dataToString: DataToString<T>,
    private val stringToData: StringToData<T>,
): InstantDataSource<T>, CachingDataSource<T> {

    override val data: T?
        get() = localReader.loadCachedString()
            ?.let { stringToData.create(it) }

    override fun saveNewCache(data: T) = localReader.saveNewCache(dataToString.create(data))

    override fun deleteCache() = localReader.deleteCache()
}
