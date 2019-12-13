package com.example.newsapp

import com.example.newsapp.BaseSchedulerProvider.TrampolineSchedulerProvider
import com.example.newsapp.models.NewsApiResponse.Article
import com.example.newsapp.models.NewsApiResponse.Result
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
    val article1 = Article(title = "First")
    val article2 = Article(title = "Second")
    lateinit var newsApiService: NewsApiService
    lateinit var dataInteractor: DataInteractor
    lateinit var items: List<Article?>
    var isFull: Boolean = false
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
            .thenReturn(Observable.just(Result("OK", 2, listOf(article1))))
        _when(newsApiService.getData(searchString, articlesPerPage, 2, apiKey))
            .thenReturn(Observable.just(Result("OK", 2, listOf(article2))))
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
    fun shouldCreateDataInteractorWithNewSearchString() {
        val newSearchString = "newSearchString"

        dataInteractor = dataInteractor.withSearchString(newSearchString)

        assertEquals(newSearchString, dataInteractor.searchString)
        verifyNoMoreInteractions(newsApiService)
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

        assertEquals(listOf<Article>(), items)
        assertEquals(expectedErrorMessage, errorMessage)
    }

    @Test
    fun shouldFailFetchingWhenSearchReturnsNoResults() {
        val expectedErrorMessage = "No articles found for search key: ${searchString}"
        _when(newsApiService.getData(searchString, articlesPerPage, 1, apiKey))
            .thenReturn(Observable.just(Result("OK", 0, listOf())))
        items = listOf(article1)
        errorMessage = null

        dataInteractor.firstPage(this::onItemsLoaded, this::onError)

        assertEquals(listOf<Article>(), items)
        assertEquals(expectedErrorMessage, errorMessage)
    }

    private fun onItemsLoaded(items: List<Article?>, isFull: Boolean) {
        this.items = items
        this.isFull = isFull
    }

    private fun onError(errorMessage: String?) {
        this.errorMessage = errorMessage
    }
}
