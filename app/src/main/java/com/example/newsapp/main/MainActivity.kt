package com.example.newsapp.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.ArticleDetailsFragment
import com.example.newsapp.R
import com.example.newsapp.models.NewsApiResponse

class MainActivity : AppCompatActivity(), ArticleListFragment.OnArticleSelected {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val listFragment = ArticleListFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.root_layout, listFragment, "articleList")
                .commit()
        }
    }

    override fun onArticleSelected(article: NewsApiResponse.Article) {
        val detailsFragment = ArticleDetailsFragment.newInstance(article)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root_layout, detailsFragment, "articleDetails")
            .addToBackStack(null)
            .commit()
    }
}
