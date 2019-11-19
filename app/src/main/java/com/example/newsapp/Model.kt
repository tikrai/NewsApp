package com.example.newsapp

import java.io.Serializable

object Model {
    data class Result(
        val status: String = "ok",
        val totalResults: Int = 1,
        val articles: List<Article> = listOf(Article())
    )

    data class Article (
        val source : Source = Source(),
        val author : String = "Author",
        val title : String = "Title",
        val description : String = "description",
        val url : String = "https://www.example.com",
        val urlToImage : String? = "https://www.example.com/image.jpg",
        val publishedAt : String = "2019-11-12T04:46:00Z",
        val content : String = "Content"
    ) : Serializable

    data class Source (
        val id : String = "SourceID",
        val name : String = "SourceName"
    ) : Serializable
}