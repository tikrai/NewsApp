package com.example.newsapp.article_list

import com.example.newsapp.DataInteractor
import com.example.newsapp.models.NewsApiResponse.Article

class ListPresenter(
    var listView: ListView?,
    private var dataInteractor: DataInteractor
) {

    fun onRefresh() {
        dataInteractor.firstPage(this::onItemsLoaded, this::onError)
    }

    fun onRefresh(newDataInteractor: DataInteractor) {
        dataInteractor = newDataInteractor
        dataInteractor.firstPage(this::onItemsLoaded, this::onError)
    }

    private fun onItemsLoaded(items: List<Article?>, isFull: Boolean) {
        listView?.apply {
            setItems(items, isFull)
            hideProgress()
        }
    }

    private fun onError(errorMessage: String?) {
        listView?.apply {
            showError(errorMessage)
            hideProgress()
        }
    }

    fun onScrollToBottom() {
        dataInteractor.nextPage(this::onItemsLoaded, this::onError)
    }

    fun onItemClicked(item: Article) {
        listView?.loadArticle(item)
    }

    fun onDestroy() {
        listView = null
    }
}
