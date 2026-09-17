package io.github.xjet.newsdemo

/** Offline fallback so the demo always shows something when the network is down. */
object SampleNews {
    val items = listOf(
        NewsItem(1, "XJet 2.2 现在是一个单依赖的 MVVM Android 框架", "yabinlee3", 120, 34, "https://github.com/yabinlee3-sketch/xjet"),
        NewsItem(2, "Compose 与 XML 统一走 View → ViewModel → Repository", "xjet", 88, 21, "https://github.com/yabinlee3-sketch/xjet"),
        NewsItem(3, "Kotlin Flow 让四态 UI 变成一种日常", "coroutines", 66, 9, "https://kotlinlang.org"),
        NewsItem(4, "为什么 MeshCentral 出现在固定设备的通信列表里", "sysadmin", 204, 51, "https://news.ycombinator.com"),
        NewsItem(5, "把老式 Android 项目迁移到 MVVM 的一次实践", "android", 41, 7, "https://developer.android.com"),
    )
}
