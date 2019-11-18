package com.example.newsapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.include_list.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), RecyclerAdapter.Listener {

    companion object {
        const val ARTICLE = "article"
        var density = 1f

        fun formatDateTime(isoFormatted: String): String {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
            val formatter = SimpleDateFormat("yyyy-MMMM-dd HH:mm", Locale.ENGLISH)
            return try {
                formatter.format(parser.parse(isoFormatted))
            } catch (e: Exception) {
                isoFormatted
            }
        }

        fun dp(dp: Int) = (dp * density).toInt()
    }

    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var disposable: CompositeDisposable

    private val newsApiServe by lazy {
        NewsApiService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        density = resources.displayMetrics.density
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRecyclerView()
        loadData()

        swipe.setOnRefreshListener {
            loadData()
        }
    }

    private fun initRecyclerView() {
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = layoutManager
    }

    private fun loadData() {
        val searchString = "Trump" //todo replace with customizable string
        val from = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
        val sortBy = "publishedAt"
        val apiKey = "64e9fccefc4e42808fd3035c23e8a490"

        disposable = CompositeDisposable()
        disposable.add(newsApiServe
            .getData(searchString, from, sortBy, apiKey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> handleResponse(result) },
                { error -> showError(error.message) }
            )
        )
    }

    private fun handleResponse(result: Model.Result) {
        recyclerAdapter = RecyclerAdapter(result.articles, this)
        recycler_view.adapter = recyclerAdapter
        swipe.isRefreshing = false
    }

    private fun showError(error: Any?) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    override fun onItemClick(article: Model.Article) {
        val intent = Intent(this, ArticleViewActivity::class.java)
        intent.putExtra(ARTICLE, article)
        startActivity(intent)
    }
}
