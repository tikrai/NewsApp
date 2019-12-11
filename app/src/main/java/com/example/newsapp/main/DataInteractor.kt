package com.example.newsapp.main

import com.example.newsapp.BaseSchedulerProvider
import com.example.newsapp.models.NewsApiResponse
import io.reactivex.observers.DisposableObserver

class DataInteractor(
    private val newsApiService: NewsApiService,
    private val scheduleProvider: BaseSchedulerProvider,
    private val articlesPerPage: Int,
    private val apiKey: String,
    val searchString: String
) {
    private var contents: ArrayList<NewsApiResponse.Article?> = ArrayList()
    private var pagesLoaded = 0
    private var totalArticles = 0

    fun firstPage(
        onNext: (ArrayList<NewsApiResponse.Article?>) -> Unit,
        onError: (String?) -> Unit
    ) {
        pagesLoaded = 0
        loadPage(1, onNext, onError)
    }

    fun nextPage(
        onNext: (ArrayList<NewsApiResponse.Article?>) -> Unit,
        onError: (String?) -> Unit
    ) {
        loadPage(pagesLoaded + 1, onNext, onError)
    }

    private fun loadPage(
        pageToLoad: Int,
        onNext: (ArrayList<NewsApiResponse.Article?>) -> Unit,
        onError: (String?) -> Unit
    ) {
        newsApiService
            .getData(searchString, articlesPerPage, pageToLoad, apiKey)
            .subscribeOn(scheduleProvider.io())
            .observeOn(scheduleProvider.ui())
            .subscribe(getObserver(onNext, onError))
    }

    private fun getObserver(
        onNext: (ArrayList<NewsApiResponse.Article?>) -> Unit,
        onError: (String?) -> Unit
    ): DisposableObserver<NewsApiResponse.Result> {
        return object : DisposableObserver<NewsApiResponse.Result>() {

            override fun onNext(result: NewsApiResponse.Result) {
                if (pagesLoaded == 0) {
                    contents = ArrayList(result.articles)
                } else {
                    contents.addAll(result.articles)
                }
                pagesLoaded++
                totalArticles = result.totalResults
                if (totalArticles == 0) {
                    onError("No articles found for search key \"${searchString}\"")
                }
                onNext(contents)
            }

            override fun onError(e: Throwable) {
                onError(e.message)
            }

            override fun onComplete() {}
        }
    }

    fun isFullyLoaded(): Boolean {
        return totalArticles <= contents.size
    }

    fun last() = contents.size - 1

    fun withSearchString(searchString: String): DataInteractor =
        DataInteractor(newsApiService, scheduleProvider, articlesPerPage, apiKey, searchString)
}
