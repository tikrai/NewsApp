package com.example.newsapp

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
import kotlinx.android.synthetic.main.include_list.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), RecyclerAdapter.Listener, DataSet.Listener {
    companion object {
        const val ARTICLE = "article"
        const val SEARCH_STRING = "searchString"
        const val ITEMS_PER_PAGE = 10

        var density = 1f

        fun formatDateTime(isoFormatted: String): String {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
            val formatter = SimpleDateFormat("yyyy-MMMM-dd HH:mm", Locale.ENGLISH)
            return try {
                val parsed = parser.parse(isoFormatted) ?: return isoFormatted
                formatter.format(parsed)
            } catch (e: Exception) {
                isoFormatted
            }
        }
    }

    private lateinit var dataSet: DataSet
    private lateinit var settings: SharedPreferences
    private lateinit var searchString: String
    private var isLoading = false

    private val newsApiService by lazy { NewsApiService.create() }
    private val recyclerAdapter by lazy { RecyclerAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        density = resources.displayMetrics.density
        settings = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        searchString = settings.getString(SEARCH_STRING, getString(R.string.defaultSearchString)) ?: ""

        setSupportActionBar(toolbar)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerAdapter

        val apiKey = getString(R.string.apiKey)
        dataSet = DataSet(this, newsApiService, recyclerAdapter, apiKey, searchString)
        dataSet.loadFirstPage()
        swipe.setOnRefreshListener {
            dataSet.loadFirstPage()
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
                    dataSet.loadNextPage()
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
                settings.edit().putString(SEARCH_STRING, query).apply()
                searchString = query
                dataSet.loadFirstPage()
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

    override fun onItemClick(article: Model.Article) {
        val intent = Intent(this, ArticleViewActivity::class.java)
        intent.putExtra(ARTICLE, article)
        startActivity(intent)
    }
}
