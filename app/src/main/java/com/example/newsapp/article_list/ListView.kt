package com.example.newsapp.article_list

import com.example.newsapp.models.NewsApiResponse.Article

interface ListView {
    fun showProgress()
    fun hideProgress()
    fun setItems(items: List<Article?>, isFull: Boolean)
    fun showError(errorMessage: String?)
    fun loadArticle(article: Article)
}
