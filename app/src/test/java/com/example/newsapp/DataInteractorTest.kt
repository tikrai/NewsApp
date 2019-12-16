package com.example.newsapp

import com.example.newsapp.BaseSchedulerProvider.SchedulerProvider
import com.example.newsapp.BaseSchedulerProvider.TrampolineSchedulerProvider
import com.example.newsapp.models.NewsApiResponse.Article
import com.example.newsapp.models.NewsApiResponse.Result
import io.reactivex.Observable
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.anyString
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when` as _when

class DataInteractorTest {
    private val articlesPerPage = 1
    private val apiKey = "apiKey"
    private val searchString = "searchString"
    private val article1 = Article(title = "First")
    private val article2 = Article(title = "Second")
    private lateinit var newsApiService: NewsApiService
    private lateinit var dataInteractor: DataInteractor

    private lateinit var items: List<Article?>
    private var isFull: Boolean = false
    private var errorMessage: String? = null

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
        _when(newsApiService.getData(anyString(), anyInt(), eq(1), anyString()))
            .thenReturn(Observable.just(Result("OK", 2, listOf(article1))))
        _when(newsApiService.getData(anyString(), anyInt(), eq(2), anyString()))
            .thenReturn(Observable.just(Result("OK", 2, listOf(article2))))
    }

    @After
    fun verifyMocks() {
        verifyNoMoreInteractions(newsApiService)
    }

    @Test(expected = ExceptionInInitializerError::class)
    fun shouldFailIfNoTrampolineScheduleProviderIsUsedForTest() {
        dataInteractor = DataInteractor(
            newsApiService,
            SchedulerProvider(),
            articlesPerPage,
            apiKey,
            searchString
        )
        try {
            dataInteractor.loadPage({ _, _ -> }, { })
        } finally {
            verify(newsApiService).getData(searchString, articlesPerPage, 1, apiKey)
        }
    }

    @Test
    fun shouldContainOneArticleAfterFetchingFirstPage() {
        setExpectedResults(listOf(article1), false)
        dataInteractor.loadPage(this::assertItemsLoaded, this::assertErrorMessage)
        verify(newsApiService).getData(searchString, articlesPerPage, 1, apiKey)
    }

    @Test
    fun shouldContainTwoArticlesAfterFetchingFirstAndSecondPage() {
        dataInteractor.loadPage({ _, _ -> }, { })
        setExpectedResults(listOf(article1, article2), true)

        dataInteractor.loadPage(this::assertItemsLoaded, this::assertErrorMessage)

        verify(newsApiService).getData(searchString, articlesPerPage, 1, apiKey)
        verify(newsApiService).getData(searchString, articlesPerPage, 2, apiKey)
    }

    @Test
    fun shouldFailFetchingPage() {
        val expectedErrorMessage = "error"
        _when(newsApiService.getData(searchString, articlesPerPage, 1, apiKey))
            .thenReturn(Observable.error(Exception(expectedErrorMessage)))
        setExpectedError(expectedErrorMessage)

        dataInteractor.loadPage(this::assertItemsLoaded, this::assertErrorMessage)

        verify(newsApiService).getData(searchString, articlesPerPage, 1, apiKey)
    }

    @Test
    fun shouldFailFetchingWhenSearchReturnsNoResults() {
        val expectedErrorMessage = "No articles found for search key: $searchString"
        _when(newsApiService.getData(searchString, articlesPerPage, 1, apiKey))
            .thenReturn(Observable.just(Result("OK", 0, listOf())))
        setExpectedError(expectedErrorMessage)

        dataInteractor.loadPage(this::assertItemsLoaded, this::assertErrorMessage)

        verify(newsApiService).getData(searchString, articlesPerPage, 1, apiKey)
    }

    @Test
    fun shouldCreateDataInteractorWithNewSearchString() {
        val newSearchString = "newSearchString"

        dataInteractor = dataInteractor.withSearchString(newSearchString)

        dataInteractor.loadPage({ _, _ -> }, { })
        verify(newsApiService).getData(newSearchString, articlesPerPage, 1, apiKey)
    }

    private fun setExpectedResults(items: List<Article?>, isFull: Boolean) {
        this.items = items
        this.isFull = isFull
    }

    private fun setExpectedError(errorMessage: String?) {
        this.errorMessage = errorMessage
    }

    private fun assertItemsLoaded(items: List<Article?>, isFull: Boolean) {
        assertEquals(this.items, items)
        assertEquals(this.isFull, isFull)
    }

    private fun assertErrorMessage(errorMessage: String?) {
        assertEquals(this.errorMessage, errorMessage)
    }
}
