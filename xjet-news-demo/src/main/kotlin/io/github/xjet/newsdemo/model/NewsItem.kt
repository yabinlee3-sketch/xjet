package io.github.xjet.newsdemo.model

import java.io.Serializable

/** One headline shown in the list and detail screens. */
data class NewsItem(
    val id: Long,
    val title: String,
    val by: String = "",
    val score: Int = 0,
    val comments: Int = 0,
    val url: String = "",
) : Serializable
