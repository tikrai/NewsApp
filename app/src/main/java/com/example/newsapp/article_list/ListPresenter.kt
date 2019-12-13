package com.example.newsapp.article_list

import com.example.newsapp.DataInteractor
import com.example.newsapp.models.NewsApiResponse.Article

class ListPresenter(
    private var listView: ListView?,
    var dataInteractor: DataInteractor
) {

    fun loadData() {
        dataInteractor.loadPage(this::onItemsLoaded, this::onError)
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

    fun onLastItemShown() {
        loadData()
    }

    fun onItemClicked(item: Article) {
        listView?.loadArticle(item)
    }

    fun onDestroy() {
        listView = null
    }

    fun setSearchString(searchString: String): ListPresenter {
        dataInteractor = dataInteractor.withSearchString(searchString)
        return this
    }

    fun resetInteractor(): ListPresenter = setSearchString(dataInteractor.searchString)
}
