package com.example.newsapp

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity.CENTER
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.setPadding
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import androidx.core.view.setMargins
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val ARTICLE = "article"

        fun formatDateTime(isoFormatted: String): String {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
            val formatter = SimpleDateFormat("yyyy-MMMM-dd HH:mm", Locale.ENGLISH)
            return try {
                formatter.format(parser.parse(isoFormatted))
            } catch (e: Exception) {
                isoFormatted
            }
        }
    }

    private var disposable: Disposable? = null

    private val newsApiServe by lazy {
        NewsApiService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadContents()
        swipe.setOnRefreshListener {
            loadContents()
            swipe.isRefreshing = false
        }
    }

    private fun loadContents() {
        if (edit_search.text.toString().isNotEmpty()) {
            beginSearch(edit_search.text.toString())
        }
    }

    private fun beginSearch(searchstring: String) {
        val from = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
        val sortBy = "publishedAt"
        val apiKey = "64e9fccefc4e42808fd3035c23e8a490"

        disposable = newsApiServe
            .hitCountCheck(searchstring, from, sortBy, apiKey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> showResult(result) },
                { error -> showError(error.message) }
            )
    }

    private fun showResult(result: Model.Result) {
        txt_search_result.text = "Total results ${result.totalResults}"

        contents.removeAllViews()
        result.articles.forEach {
            displayArticle(it)
        }
    }

    private fun displayArticle(article: Model.Article) {
        val paddingSize = 10
        val marginSize = 3
        val imageWidth = 150

        val articleImageView = ImageView(this)

        try {
            Picasso.with(this)
                .load(article.urlToImage ?: "")
                .resize(dp(imageWidth), 0)
                .error(R.drawable.no_image_available)
                .into(articleImageView)
        } catch (e: Exception) {
            articleImageView.setBackgroundResource(R.drawable.no_image_available)
        }

        val articleTitleView = TextView(this)
        articleTitleView.setTextColor(Color.BLACK)
        articleTitleView.text = article.title

        val articleDateView = TextView(this)
        articleDateView.gravity = CENTER
        articleDateView.text = formatDateTime(article.publishedAt)

        val articleTextLayout = LinearLayout(this)
        articleTextLayout.orientation = LinearLayout.VERTICAL
        articleTextLayout.setPadding(dp(paddingSize), 0, 0, 0)

        articleTextLayout.addView(articleTitleView)
        articleTextLayout.addView(articleDateView)
        articleDateView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        val articleLayout = LinearLayout(this)
        articleLayout.orientation = LinearLayout.HORIZONTAL
        articleLayout.setBackgroundResource(R.drawable.rounded_corner)
        articleLayout.setPadding(dp(paddingSize))

        val articleLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        articleLayoutParams.setMargins(dp(marginSize))
        articleLayout.setLayoutParams(articleLayoutParams)

        articleLayout.addView(articleImageView)
        articleLayout.addView(articleTextLayout)
        contents.addView(articleLayout)
        articleLayout.setOnClickListener{
            val intent = Intent(this, ArticleViewActivity::class.java)
            intent.putExtra(ARTICLE, article)
            startActivity(intent)
        }
    }

    private fun showError(error: Any?) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun dp(dp: Int) = (dp * resources.displayMetrics.density).toInt()

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }
}
