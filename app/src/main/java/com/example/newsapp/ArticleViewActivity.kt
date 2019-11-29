package com.example.newsapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_article_view.*
import android.content.Intent
import android.net.Uri

class ArticleViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_view)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val article_key = resources.getString(R.string.intent_extra_key_article)
        val article = intent.getSerializableExtra(article_key) as Model.Article

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
        dateView.text = MainActivity.formatDateTime(article.publishedAt)
        button.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)))
        }
    }
}
