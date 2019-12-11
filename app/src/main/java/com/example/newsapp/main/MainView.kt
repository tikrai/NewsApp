package com.example.newsapp.main

import com.example.newsapp.models.NewsApiResponse.Article

interface MainView {
    fun showProgress()
    fun hideProgress()
    fun setItems(items: List<Article?>, isFull: Boolean)
    fun showError(errorMessage: String?)
    fun loadArticle(article: Article)
}
