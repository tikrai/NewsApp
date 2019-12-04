package com.example.newsapp.main

import com.example.newsapp.models.NewsApiResponse

class MainPresenter(var mainView: MainView?, private val dataInteractor: DataInteractor) {

    fun onRefresh() {
        dataInteractor.firstPage(this::onItemsLoaded, this::onError)
    }

    private fun onItemsLoaded(items: List<NewsApiResponse.Article?>) {
        mainView?.apply {
            setItems(items)
            hideProgress()
        }
    }

    private fun onError(errorMessage: String?) {
        mainView?.apply {
            showError(errorMessage)
            hideProgress()
        }
    }

    fun onScrollToBottom() {
        mainView?.showProgress()
        dataInteractor.nextPage(this::onItemsLoaded, this::onError)
    }

    fun onItemClicked(item: NewsApiResponse.Article) {
        mainView?.loadArticle(item)
    }

    fun onDestroy() {
        mainView = null
    }
}
