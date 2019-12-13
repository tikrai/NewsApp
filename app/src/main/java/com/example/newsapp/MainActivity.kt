package com.example.newsapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.article_details.ArticleDetailsFragment
import com.example.newsapp.article_list.ArticleListFragment
import com.example.newsapp.models.NewsApiResponse.Article

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

    override fun onArticleSelected(article: Article) {
        val detailsFragment = ArticleDetailsFragment.newInstance(article)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root_layout, detailsFragment, "articleDetails")
            .addToBackStack(null)
            .commit()
    }
}
