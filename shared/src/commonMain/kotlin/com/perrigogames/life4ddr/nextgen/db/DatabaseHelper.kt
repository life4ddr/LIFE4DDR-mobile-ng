package com.perrigogames.life4ddr.nextgen.db

import app.cash.sqldelight.db.SqlDriver
import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.Life4Db
import com.perrigogames.life4ddr.nextgen.data.StableIdColumnAdapter
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.GoalStatus
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import com.perrigogames.life4ddr.nextgen.enums.ResultSource
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank

abstract class DatabaseHelper(
    private val sqlDriver: SqlDriver,
    protected val logger: Logger,
) {
    protected val dbRef = Life4Db(sqlDriver,
        ChartResult.Adapter(
            StableIdColumnAdapter(DifficultyClass.entries.toTypedArray()),
            StableIdColumnAdapter(PlayStyle.entries.toTypedArray()),
            StableIdColumnAdapter(ClearType.entries.toTypedArray()),
            StableIdColumnAdapter(ResultSource.entries.toTypedArray())
        ),
        GoalState.Adapter(
            StableIdColumnAdapter(GoalStatus.entries.toTypedArray())
        ),
        TrialSession.Adapter(
            StableIdColumnAdapter(TrialRank.entries.toTypedArray())
        )
    )

    internal fun dbClear() {
        sqlDriver.close()
    }
}