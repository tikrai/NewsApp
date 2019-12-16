package com.example.newsapp.article_list

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.newsapp.BaseSchedulerProvider
import com.example.newsapp.DataInteractor
import com.example.newsapp.NewsApiService
import com.example.newsapp.R
import com.example.newsapp.SavedPreferenceString
import com.example.newsapp.SearchString
import com.example.newsapp.models.NewsApiResponse.Article
import kotlinx.android.synthetic.main.fragment_article_list.swipe
import kotlinx.android.synthetic.main.fragment_article_list.view.list_view
import kotlinx.android.synthetic.main.fragment_article_list.view.swipe
import kotlinx.android.synthetic.main.fragment_article_list.view.toolbar

class ArticleListFragment : Fragment(), SearchBarView, ListView {
    private lateinit var listener: OnArticleSelected
    private val newsApiService by lazy {
        NewsApiService.create(resources.getString(R.string.base_url))
    }
    private lateinit var searchString: SavedPreferenceString
    private lateinit var searchBarPresenter: SearchBarPresenter
    private lateinit var listPresenter: ListPresenter
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
        searchString = SearchString(context)

        searchBarPresenter = SearchBarPresenter(this, searchString)

        val dataInteractor = DataInteractor(
            newsApiService,
            BaseSchedulerProvider.SchedulerProvider(),
            resources.getInteger(R.integer.articles_per_page),
            resources.getString(R.string.api_key),
            searchString.load()
        )

        listPresenter = ListPresenter(this, dataInteractor)
        listAdapter = ListAdapter(listPresenter::onItemClicked, listPresenter::onLastItemShown)
        listPresenter.loadData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_article_list, container, false)

        view.toolbar.inflateMenu(R.menu.menu_toolbar)
        searchBarPresenter.initMenuListener(view.toolbar.menu)

        view.list_view.adapter = listAdapter

        view.swipe.setOnRefreshListener {
            listPresenter.setSearchString(searchString.load()).loadData()
        }
        return view
    }

    override fun onDestroy() {
        listPresenter.onDestroy()
        super.onDestroy()
    }

    override fun setSearchString(searchString: String) {
        listPresenter.setSearchString(searchString).loadData()
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
        listAdapter.setLoadingIsFinished()
    }

    override fun loadArticle(article: Article) {
        listener.onArticleSelected(article)
    }

    interface OnArticleSelected {
        fun onArticleSelected(article: Article)
    }
}
