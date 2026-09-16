package io.github.xjet.sample

import android.app.Application
import io.github.xjet.annotation.SpiService
import io.github.xjet.core.XJet
import io.github.xjet.core.XJetConfig
import io.github.xjet.room.RoomDatabaseProvider

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val database = RoomDatabaseProvider.create(this, AppDatabase::class.java, "xjet-sample.db")
        XJet.init(
            this,
            XJetConfig.Builder(this)
                .debug(true)
                .autoDiscoverConfig(true)
                .database(database)
                .logTag("XJetSample")
                .build()
        )
    }
}

interface GreetingService {
    fun greet(): String
}

@SpiService(api = GreetingService::class)
class SampleGreetingService : GreetingService {
    override fun greet(): String = "Hello from KSP-registered @SpiService"
}

interface ConfigGreetingService {
    fun greet(): String
}

class SampleConfigGreetingService : ConfigGreetingService {
    override fun greet(): String = "Hello from assets/xjet/spi.properties"
}
