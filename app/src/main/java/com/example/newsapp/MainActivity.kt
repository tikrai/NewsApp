package com.example.newsapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.include_list.*
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.content.SharedPreferences
import android.view.MenuItem

class MainActivity : AppCompatActivity(), RecyclerAdapter.Listener {

    companion object {
        const val ARTICLE = "article"
        const val SEARCH_STRING = "searchString"
        const val ITEMS_PER_PAGE = 10

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
    private lateinit var settings: SharedPreferences
    private lateinit var searchString: String
    private var pagesLoaded = 0
    private var isLoading = false

    private var articleList = ArrayList<Model.Article?>()
    private var totalResults = 0

    private val newsApiService by lazy {
        NewsApiService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        density = resources.displayMetrics.density
        settings = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        searchString = settings.getString(SEARCH_STRING, getString(R.string.defaultSearchString)) ?: ""

        setSupportActionBar(toolbar)
        initRecyclerView()
        loadData()
        swipe.setOnRefreshListener {
            loadData()
        }
        initScrollListener()
    }

    private fun initRecyclerView() {
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerAdapter = RecyclerAdapter(articleList, this)
        recyclerView.adapter = recyclerAdapter
    }

    private fun appendData() {
        val apiKey = getString(R.string.apiKey)
        disposable = CompositeDisposable()
        disposable.add(newsApiService
            .getData(searchString, ITEMS_PER_PAGE, pagesLoaded + 1, apiKey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> handleResponse(result) },
                { error -> showError(error.message) }
            )
        )
    }

    private fun loadData() {
        articleList.clear()
        recyclerAdapter.notifyDataSetChanged()
        pagesLoaded = 0
        appendData()
    }

    private fun handleResponse(result: Model.Result) {
        pagesLoaded++
        ensureNoNullElements()
        articleList.addAll(result.articles)
        recyclerAdapter.notifyDataSetChanged()
        totalResults = result.totalResults
        swipe.isRefreshing = false
        isLoading = false
    }

    private fun ensureNoNullElements() {
        if (!articleList.isEmpty() && articleList[articleList.size - 1] == null) {
            articleList.removeAt(articleList.size - 1)
            recyclerAdapter.notifyDataSetChanged()
        }
    }

    private fun showError(error: Any?) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
        swipe.isRefreshing = false
        ensureNoNullElements()
    }

    private fun initScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (!isLoading
                    && totalResults > pagesLoaded * ITEMS_PER_PAGE
                    && layoutManager.findLastCompletelyVisibleItemPosition() == articleList.size - 1
                ) {
                    isLoading = true
                    articleList.add(null)
                    recyclerAdapter.notifyDataSetChanged()
                    appendData()
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
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
                loadData()
                return false
            }

            override fun onQueryTextChange(newText: String) = true
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onItemClick(article: Model.Article) {
        val intent = Intent(this, ArticleViewActivity::class.java)
        intent.putExtra(ARTICLE, article)
        startActivity(intent)
    }
}
