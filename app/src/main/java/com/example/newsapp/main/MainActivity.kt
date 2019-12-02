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

class MainActivity : AppCompatActivity(), RecyclerAdapter.Listener, DataSet.Listener {

    private lateinit var dataSet: DataSet
    private lateinit var settings: SharedPreferences
    private lateinit var searchString: String
    private var isLoading = false

    private val newsApiService by lazy { NewsApiService.create() }
    private val recyclerAdapter by lazy { RecyclerAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settings = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        searchString = settings.getString(
            resources.getString(R.string.search_string_key),
            resources.getString(R.string.search_string_default)
        ) ?: ""

        setSupportActionBar(toolbar)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerAdapter

        dataSet = DataSet(
            this,
            newsApiService,
            recyclerAdapter,
            resources.getInteger(R.integer.articles_per_page),
            resources.getString(R.string.api_key)
        )
        dataSet.loadFirstPage(searchString)
        swipe.setOnRefreshListener {
            dataSet.loadFirstPage(searchString)
        }
        initScrollListener()
    }

    private fun initScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (!isLoading
                    && !dataSet.isFullyLoaded()
                    && layoutManager.findLastCompletelyVisibleItemPosition() == dataSet.last()
                ) {
                    isLoading = true
                    dataSet.loadNextPage(searchString)
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        dataSet.onPause()
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
                dataSet.loadFirstPage(searchString)
                return false
            }

            override fun onQueryTextChange(newText: String) = true
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onDataLoaded() {
        swipe.isRefreshing = false
        isLoading = false
    }

    override fun onError(errorMessage: String?) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        swipe.isRefreshing = false
    }

    override fun onItemClick(article: NewsApiResponse.Article) {
        val intent = Intent(this, ArticleViewActivity::class.java)
        intent.putExtra(resources.getString(R.string.intent_extra_key_article), article)
        startActivity(intent)
    }
}
