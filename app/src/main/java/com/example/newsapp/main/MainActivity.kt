package com.example.newsapp.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.*
import com.example.newsapp.article.ArticleViewActivity
import com.example.newsapp.models.NewsApiResponse
import kotlinx.android.synthetic.main.include_list.*

class MainActivity : AppCompatActivity(), MainView {
    private lateinit var dataInteractor: DataInteractor
    private lateinit var settings: SharedPreferences
    private lateinit var searchString: String
    private var isLoading = false

    private val newsApiService by lazy { NewsApiService.create() }
    private lateinit var presenter: MainPresenter
    private lateinit var mainAdapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settings = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        searchString = settings.getString(
            resources.getString(R.string.search_string_key),
            resources.getString(R.string.search_string_default)
        ) ?: ""

        initMainView(searchString)
        setSupportActionBar(toolbar)
        swipe.setOnRefreshListener {
            presenter.onResume()
        }
        initScrollListener()
    }

    private fun initMainView(searchString: String) {
        dataInteractor = DataInteractor(
            newsApiService,
            SchedulerProvider(),
            resources.getInteger(R.integer.articles_per_page),
            resources.getString(R.string.api_key),
            searchString
        )
        presenter = MainPresenter(this, dataInteractor)
        mainAdapter = MainAdapter(presenter::onItemClicked)
        listView.adapter = mainAdapter
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    override fun showProgress() {
        mainAdapter.setLoading()
        swipe.isRefreshing = true
    }

    override fun hideProgress() {
        swipe.isRefreshing = false
    }

    override fun setItems(items: List<NewsApiResponse.Article?>) {
        mainAdapter.setItems(items)
        isLoading = false
    }

    override fun showError(errorMessage: String?) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        swipe.isRefreshing = false
        mainAdapter.setLoading(false)
    }

    override fun loadArticle(article: NewsApiResponse.Article) {
        val intent = Intent(this, ArticleViewActivity::class.java)
        intent.putExtra(resources.getString(R.string.intent_extra_key_article), article)
        startActivity(intent)
    }

    private fun initScrollListener() {
        listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (!isLoading
                    && !dataInteractor.isFullyLoaded()
                    && layoutManager.findLastCompletelyVisibleItemPosition() >= dataInteractor.last()
                ) {
                    isLoading = true
                    presenter.onScrollToBottom()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        val mSearch = menu.findItem(R.id.search)
        val searchView = mSearch.actionView as SearchView

        mSearch.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchView.onActionViewExpanded()
                searchView.setQuery(searchString, false)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?) = true
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                settings.edit()
                    .putString(resources.getString(R.string.search_string_key), query)
                    .apply()
                searchString = query
                presenter.onDestroy()
                initMainView(searchString)
                presenter.onResume()
                return false
            }

            override fun onQueryTextChange(newText: String) = true
        })

        return super.onCreateOptionsMenu(menu)
    }
}
