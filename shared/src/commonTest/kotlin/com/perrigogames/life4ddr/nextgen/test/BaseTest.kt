package com.perrigogames.life4ddr.nextgen.test

import com.perrigogames.life4ddr.nextgen.loggerModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

open class BaseTest : KoinTest {

    protected val dispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(dispatcher)
    }

    @BeforeTest
    fun setupKoin() {
        startKoin {
            modules(loggerModule)
        }
    }

    @AfterTest
    fun teardownKoin() {
        stopKoin()
    }
}