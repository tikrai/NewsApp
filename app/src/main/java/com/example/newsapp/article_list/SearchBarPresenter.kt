package com.example.newsapp.article_list

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import com.example.newsapp.R
import com.example.newsapp.SavedPreferenceString

class SearchBarPresenter(
    private val searchBarView: SearchBarView,
    private val searchString: SavedPreferenceString
) {

    fun initMenuListener(menu: Menu) {
        val menuItem = menu.findItem(R.id.search)
        val searchView = menuItem.actionView as SearchView

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchView.onActionViewExpanded()
                searchView.setQuery(searchString.load(), false)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?) = true
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchString.save(query)
                searchBarView.setSearchString(query)
                return false
            }

            override fun onQueryTextChange(newText: String) = true
        })
    }
}
