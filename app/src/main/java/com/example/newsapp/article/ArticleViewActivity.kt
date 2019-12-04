package com.example.newsapp.article

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.R
import com.example.newsapp.Utils.Companion.formatDateTime
import com.example.newsapp.models.NewsApiResponse
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_article_view.articleToolbar
import kotlinx.android.synthetic.main.activity_article_view.authorView
import kotlinx.android.synthetic.main.activity_article_view.button
import kotlinx.android.synthetic.main.activity_article_view.dateView
import kotlinx.android.synthetic.main.activity_article_view.descriptionView
import kotlinx.android.synthetic.main.activity_article_view.imageView
import kotlinx.android.synthetic.main.activity_article_view.titleView

class ArticleViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_view)

        setSupportActionBar(articleToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val article_key = resources.getString(R.string.intent_extra_key_article)
        val article = intent.getSerializableExtra(article_key) as NewsApiResponse.Article

        try {
            Picasso.with(this)
                .load(article.urlToImage ?: "")
                .error(R.drawable.no_image_available)
                .into(imageView)
        } catch (e: Exception) {
            imageView.setBackgroundResource(R.drawable.no_image_available)
        }
        titleView.text = article.title
        descriptionView.text = article.description
        authorView.text = article.author
        dateView.text = formatDateTime(article.publishedAt)
        button.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)))
        }
    }
}
