package com.example.newsapp

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class DataSet(
    private val listener : Listener,
    private val newsApiService: NewsApiService,
    private val recyclerAdapter: RecyclerAdapter,
    private val apiKey: String,
    private val searchString: String
) {
    private var contents: ArrayList<Model.Article?> = ArrayList()
    private var pagesLoaded = 0
    private var totalArticles = 0

    private lateinit var disposable: CompositeDisposable

    interface Listener {
        fun onDataLoaded()
        fun onError(errorMessage: String?)
    }

    fun loadFirstPage() {
        loadPage(1)
    }

    fun loadNextPage() {
        contents.add(null)
        recyclerAdapter.notifyDataSetChanged()
        loadPage(pagesLoaded + 1)
    }

    private fun loadPage(pageToLoad: Int) {
        disposable = CompositeDisposable()
        disposable.add(newsApiService
            .getData(searchString, MainActivity.ITEMS_PER_PAGE, pageToLoad, apiKey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> handleResponse(result, pageToLoad) },
                { error -> showError(error) }
            )
        )
    }

    private fun handleResponse(result: Model.Result, pageToLoad: Int) {
        if (pageToLoad == 1) {
            contents = ArrayList(result.articles)
            pagesLoaded = 1
            recyclerAdapter.setContents(contents)
        } else {
            ensureNoNullElements()
            contents.addAll(result.articles)
            pagesLoaded++
        }
        totalArticles = result.totalResults
        recyclerAdapter.notifyDataSetChanged()
        listener.onDataLoaded()
    }

    private fun showError(error: Throwable) {
        ensureNoNullElements()
        recyclerAdapter.notifyDataSetChanged()
        listener.onError(error.message)
    }

    fun isFullyLoaded(): Boolean {
        return totalArticles <= contents.size
    }

    fun last() = contents.size - 1

    private fun ensureNoNullElements() {
        if (contents.isNotEmpty() && contents[contents.size - 1] == null) {
            contents.removeAt(contents.size - 1)
        }
    }

    fun onPause() {
        disposable.dispose()
    }
}