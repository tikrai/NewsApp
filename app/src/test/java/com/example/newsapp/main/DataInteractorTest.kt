package com.example.newsapp.main

import com.example.newsapp.TrampolineSchedulerProvider
import com.example.newsapp.models.NewsApiResponse
import io.reactivex.Observable
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when` as _when

class DataInteractorTest {
    val articlesPerPage = 1
    val apiKey = "apiKey"
    val searchString = "searchString"
    val article1 = NewsApiResponse.Article(title = "First")
    val article2 = NewsApiResponse.Article(title = "Second")
    lateinit var newsApiService: NewsApiService
    lateinit var dataInteractor: DataInteractor
    lateinit var items: List<NewsApiResponse.Article?>
    var errorMessage: String? = null

    @Before
    fun setup() {
        newsApiService = mock(NewsApiService::class.java)
        dataInteractor = DataInteractor(
            newsApiService,
            TrampolineSchedulerProvider(),
            articlesPerPage,
            apiKey,
            searchString
        )
        items = listOf()
        errorMessage = null
        _when(newsApiService.getData(searchString, articlesPerPage, 1, apiKey))
            .thenReturn(Observable.just(NewsApiResponse.Result("OK", 2, listOf(article1))))
        _when(newsApiService.getData(searchString, articlesPerPage, 2, apiKey))
            .thenReturn(Observable.just(NewsApiResponse.Result("OK", 2, listOf(article2))))
    }

    @Test
    fun shouldContainOneArticleAfterFetchingFirstPage() {
        dataInteractor.firstPage(this::onItemsLoaded, this::onError)

        assertEquals(listOf(article1), items)
        assertEquals(null, errorMessage)
        verify(newsApiService).getData(searchString, articlesPerPage, 1, apiKey)
        verifyNoMoreInteractions(newsApiService)
    }

    @Test
    fun shouldReturnIsNotFullyLoadedAfterFetchingFirstPage() {
        dataInteractor.firstPage(this::onItemsLoaded, this::onError)
        assertEquals(false, dataInteractor.isFullyLoaded())
    }

    @Test
    fun shouldReturnLastItemIndexAfterFetchingFirstPage() {
        dataInteractor.firstPage(this::onItemsLoaded, this::onError)
        assertEquals(0, dataInteractor.last())
    }

    @Test
    fun shouldContainTwoArticlesAfterFetchingFirstAndSecondPage() {
        dataInteractor.firstPage(this::onItemsLoaded, this::onError)
        dataInteractor.nextPage(this::onItemsLoaded, this::onError)

        assertEquals(listOf(article1, article2), items)
        assertEquals(null, errorMessage)
        verify(newsApiService).getData(searchString, articlesPerPage, 1, apiKey)
        verify(newsApiService).getData(searchString, articlesPerPage, 2, apiKey)
        verifyNoMoreInteractions(newsApiService)
    }

    @Test
    fun shouldReturnIsFullyLoadedAfterFetchingFirstAndSecondPage() {
        dataInteractor.firstPage(this::onItemsLoaded, this::onError)
        dataInteractor.nextPage(this::onItemsLoaded, this::onError)

        assertEquals(true, dataInteractor.isFullyLoaded())
    }

    @Test
    fun shouldReturnLastItemIndexAfterFetchingFirstAndSecondPage() {
        dataInteractor.firstPage(this::onItemsLoaded, this::onError)
        dataInteractor.nextPage(this::onItemsLoaded, this::onError)

        assertEquals(1, dataInteractor.last())
    }

    @Test
    fun shouldFailFetchingThirdPage() {
        val expectedErrorMessage = "error"
        _when(newsApiService.getData(searchString, articlesPerPage, 3, apiKey))
            .thenReturn(Observable.error(Exception(expectedErrorMessage)))
        dataInteractor.firstPage(this::onItemsLoaded, this::onError)
        dataInteractor.nextPage(this::onItemsLoaded, this::onError)
        items = listOf()
        errorMessage = null

        dataInteractor.nextPage(this::onItemsLoaded, this::onError)

        val expectedList: List<NewsApiResponse.Article> = listOf()
        assertEquals(expectedList, items)
        assertEquals(expectedErrorMessage, errorMessage)
    }

    private fun onItemsLoaded(items: List<NewsApiResponse.Article?>) {
        this.items = items
    }

    private fun onError(errorMessage: String?) {
        this.errorMessage = errorMessage
    }
}
