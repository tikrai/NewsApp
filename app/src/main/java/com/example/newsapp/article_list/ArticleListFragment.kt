package com.example.newsapp.article_list

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.example.newsapp.BaseSchedulerProvider
import com.example.newsapp.BuildConfig
import com.example.newsapp.DataInteractor
import com.example.newsapp.NewsApiService
import com.example.newsapp.R
import com.example.newsapp.models.NewsApiResponse.Article
import kotlinx.android.synthetic.main.fragment_article_list.swipe
import kotlinx.android.synthetic.main.fragment_article_list.view.list_view
import kotlinx.android.synthetic.main.fragment_article_list.view.swipe
import kotlinx.android.synthetic.main.fragment_article_list.view.toolbar

class ArticleListFragment : Fragment(), ListView {

    private lateinit var settings: SharedPreferences
    private lateinit var listener: OnArticleSelected
    private lateinit var dataInteractor: DataInteractor
    private val newsApiService by lazy { NewsApiService.create() }
    private lateinit var presenter: ListPresenter
    private lateinit var listAdapter: ListAdapter

    companion object {
        fun newInstance(): ArticleListFragment {
            return ArticleListFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnArticleSelected) {
            listener = context
        } else {
            throw ClassCastException("$context must implement OnArticleSelected.")
        }

        settings = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

        initMainView(settings)
    }

    private fun initMainView(settings: SharedPreferences) {
        val searchString = settings.getString(
            resources.getString(R.string.search_string_key),
            resources.getString(R.string.search_string_default)
        )!!
        dataInteractor = DataInteractor(
            newsApiService,
            BaseSchedulerProvider.SchedulerProvider(),
            resources.getInteger(R.integer.articles_per_page),
            resources.getString(R.string.api_key),
            searchString
        )
        presenter = ListPresenter(this, dataInteractor)
        listAdapter = ListAdapter(presenter::onItemClicked, presenter::onScrollToBottom)
        presenter.onRefresh()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_article_list, container, false)
        view.list_view.adapter = listAdapter
        view.toolbar.inflateMenu(R.menu.menu_toolbar)
        initMenuListener(view.toolbar.menu)
        view.swipe.setOnRefreshListener { presenter.onRefresh() }
        return view
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    private fun initMenuListener(menu: Menu) {
        val menuItem = menu.findItem(R.id.search)
        val searchView = menuItem.actionView as SearchView

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchView.onActionViewExpanded()
                searchView.setQuery(dataInteractor.searchString, false)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?) = true
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                settings.edit()
                    .putString(resources.getString(R.string.search_string_key), query)
                    .apply()
                dataInteractor = dataInteractor.withSearchString(query)
                presenter.onRefresh(dataInteractor)
                return false
            }

            override fun onQueryTextChange(newText: String) = true
        })
    }

    override fun showProgress() {
        swipe.isRefreshing = true
    }

    override fun hideProgress() {
        swipe.isRefreshing = false
    }

    override fun setItems(items: List<Article?>, isFull: Boolean) {
        listAdapter.setItems(items, isFull)
    }

    override fun showError(errorMessage: String?) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        swipe.isRefreshing = false
        listAdapter.finishLoading()
    }

    override fun loadArticle(article: Article) {
        listener.onArticleSelected(article)
    }

    interface OnArticleSelected {
        fun onArticleSelected(article: Article)
    }
}
