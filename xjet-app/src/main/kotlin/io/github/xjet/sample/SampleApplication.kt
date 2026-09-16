package io.github.xjet.sample

import android.app.Application
import io.github.xjet.core.XJet
import io.github.xjet.core.XJetConfig
import io.github.xjet.room.RoomDatabaseProvider

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Runtime registration replaces old annotation processing:
        // one dependency, explicit wiring, no KSP step.
        XJet.register(GreetingService::class.java, SampleGreetingService())
        XJet.registerRoute(
            path = "main",
            target = MainActivity::class.java,
            group = "home",
            title = "XJet Sample Home",
        )
        XJet.registerRoute(
            path = "xmlScreen",
            target = XmlActivity::class.java,
            group = "xml",
            title = "XML + embedded Compose",
        )

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

class SampleGreetingService : GreetingService {
    override fun greet(): String = "Hello from runtime-registered service"
}

interface ConfigGreetingService {
    fun greet(): String
}

class SampleConfigGreetingService : ConfigGreetingService {
    override fun greet(): String = "Hello from assets/xjet/spi.properties"
}
