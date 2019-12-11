package com.example.newsapp.main

import com.example.newsapp.models.NewsApiResponse.Article

class MainPresenter(
    var mainView: MainView?,
    private var dataInteractor: DataInteractor
) {

    fun onRefresh() {
        println("onRefresh")
        dataInteractor.firstPage(this::onItemsLoaded, this::onError)
    }

    fun onRefresh(newDataInteractor: DataInteractor) {
        println("onRefresh with new data")
        dataInteractor = newDataInteractor
        dataInteractor.firstPage(this::onItemsLoaded, this::onError)
    }

    private fun onItemsLoaded(items: List<Article?>, isFull: Boolean) {
        println("onItemsLoaded")
        mainView?.apply {
            setItems(items, isFull)
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
        println("onScrollToBottom")
        dataInteractor.nextPage(this::onItemsLoaded, this::onError)
    }

    fun onItemClicked(item: Article) {
        println("onItemClicked: ${item.title}")
        mainView?.loadArticle(item)
    }

    fun onDestroy() {
        mainView = null
    }
}
