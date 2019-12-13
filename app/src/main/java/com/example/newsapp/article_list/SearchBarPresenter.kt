package com.example.newsapp.article_list

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import com.example.newsapp.BuildConfig
import com.example.newsapp.R

class SearchBarPresenter(
    private val searchBarView: SearchBarView,
    context: Context
) {
    private val settings = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    private val resources = context.resources
    var searchString: String = settings.getString(
        resources.getString(R.string.search_string_key),
        resources.getString(R.string.search_string_default)
    )!!
    private set

    fun initMenuListener(menu: Menu) {
        val menuItem = menu.findItem(R.id.search)
        val searchView = menuItem.actionView as SearchView

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
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
                searchBarView.setSearchString(query)
                return false
            }

            override fun onQueryTextChange(newText: String) = true
        })
    }
}
