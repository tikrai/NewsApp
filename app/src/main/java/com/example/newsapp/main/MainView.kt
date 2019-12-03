package com.example.newsapp.main

import com.example.newsapp.models.NewsApiResponse

interface MainView {
    fun showProgress()
    fun hideProgress()
    fun setItems(items: List<NewsApiResponse.Article?>)
    fun showError(errorMessage: String?)
    fun loadArticle(article: NewsApiResponse.Article)
}
