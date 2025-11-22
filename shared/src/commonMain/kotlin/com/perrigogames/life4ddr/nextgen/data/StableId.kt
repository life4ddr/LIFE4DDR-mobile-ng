package com.perrigogames.life4ddr.nextgen.data

//import com.squareup.sqldelight.ColumnAdapter

// TODO SQLDelight
/** A [ColumnAdapter] which maps the enum class `T` to a string in the database. */
//class StableIdColumnAdapter<T : StableId>(private val enumValues: Array<out T>) : ColumnAdapter<T, Long> {
//
//    override fun decode(databaseValue: Long): T = enumValues.first { it.stableId == databaseValue }
//    override fun encode(value: T) = value.stableId
//}

interface StableId {
    val stableId: Long
}