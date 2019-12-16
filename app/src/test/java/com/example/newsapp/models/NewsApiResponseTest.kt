package com.example.newsapp.models

import org.junit.Assert.assertEquals
import org.junit.Test

class NewsApiResponseTest {
    @Test
    fun shouldCheckAllGetters() {
        NewsApiResponse
        val result = NewsApiResponse.Result()
        assertEquals("ok", result.status)
        assertEquals(1, result.totalResults)
        assertEquals("SourceID", result.articles[0].source.id)
        assertEquals("SourceName", result.articles[0].source.name)
        assertEquals("Author", result.articles[0].author)
        assertEquals("Title", result.articles[0].title)
        assertEquals("description", result.articles[0].description)
        assertEquals("https://www.example.com", result.articles[0].url)
        assertEquals("https://www.example.com/image.jpg", result.articles[0].urlToImage)
        assertEquals("2019-11-12T04:46:00Z", result.articles[0].publishedAt)
        assertEquals("Content", result.articles[0].content)
    }
}
