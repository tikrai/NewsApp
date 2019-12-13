package com.example.newsapp

import com.example.newsapp.models.NewsApiResponse.Article
import com.example.newsapp.models.NewsApiResponse.Result
import io.reactivex.observers.DisposableObserver

class DataInteractor(
    private val newsApiService: NewsApiService,
    private val scheduleProvider: BaseSchedulerProvider,
    private val articlesPerPage: Int,
    private val apiKey: String,
    val searchString: String
) {
    private var contents: ArrayList<Article> = ArrayList()
    private var pagesLoaded = 0
    private var totalArticles = 0

    fun firstPage(
        onNext: (ArrayList<Article>, Boolean) -> Unit,
        onError: (String?) -> Unit
    ) {
        pagesLoaded = 0
        loadPage(1, onNext, onError)
    }

    fun nextPage(
        onNext: (ArrayList<Article>, Boolean) -> Unit,
        onError: (String?) -> Unit
    ) {
        loadPage(pagesLoaded + 1, onNext, onError)
    }

    private fun loadPage(
        pageToLoad: Int,
        onNext: (ArrayList<Article>, Boolean) -> Unit,
        onError: (String?) -> Unit
    ) {
        newsApiService
            .getData(searchString, articlesPerPage, pageToLoad, apiKey)
            .subscribeOn(scheduleProvider.io())
            .observeOn(scheduleProvider.ui())
            .subscribe(getObserver(onNext, onError))
    }

    private fun getObserver(
        onNext: (ArrayList<Article>, Boolean) -> Unit,
        onError: (String?) -> Unit
    ): DisposableObserver<Result> {
        return object : DisposableObserver<Result>() {
            override fun onNext(result: Result) {
                if (pagesLoaded == 0) {
                    contents = ArrayList(result.articles)
                } else {
                    contents.addAll(result.articles)
                }
                pagesLoaded++
                totalArticles = result.totalResults
                if (totalArticles == 0) {
                    val errorNoResults = "No articles found for search key: ${searchString}"
                    onError(errorNoResults)
                }
                val isFull = contents.size >= totalArticles
                onNext(contents, isFull)
            }

            override fun onError(e: Throwable) {
                onError(e.message)
            }

            override fun onComplete() {}
        }
    }

    fun withSearchString(searchString: String): DataInteractor =
        DataInteractor(newsApiService, scheduleProvider, articlesPerPage, apiKey, searchString)
}
