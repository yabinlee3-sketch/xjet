package io.github.xjet.newsdemo

import android.app.Application
import io.github.xjet.core.XJet
import io.github.xjet.core.XJetConfig

class NewsDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        XJet.registerRoute(
            path = "newsDetail",
            target = NewsDetailActivity::class.java,
            group = "news",
            title = "新闻详情",
        )
        XJet.init(
            this,
            XJetConfig.Builder(this)
                .debug(true)
                .installUncaughtErrorHandler(true)
                .logTag("XJetNews")
                .build()
        )
    }
}
