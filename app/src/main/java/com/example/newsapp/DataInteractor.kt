package com.example.newsapp

import com.example.newsapp.models.NewsApiResponse.Article
import com.example.newsapp.models.NewsApiResponse.Result
import io.reactivex.observers.DisposableObserver

class DataInteractor(
    private val newsApiService: NewsApiService,
    private val scheduleProvider: BaseSchedulerProvider,
    private val articlesPerPage: Int,
    private val apiKey: String,
    private val searchString: String
) {
    private var contents: ArrayList<Article> = ArrayList()
    private var pagesLoaded = 0
    private var totalArticles = 0

    fun loadPage(
        onNext: (ArrayList<Article>, Boolean) -> Unit,
        onError: (String?) -> Unit
    ) {
        val pageToLoad = pagesLoaded + 1
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
                if (result.totalResults == 0) {
                    val errorNoResults = "No articles found for search key: ${searchString}"
                    onError(errorNoResults)
                } else {
                    contents.addAll(result.articles)
                    pagesLoaded++
                    totalArticles = result.totalResults
                    val loadingIsFinished = contents.size >= totalArticles
                    onNext(contents, loadingIsFinished)
                }
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
