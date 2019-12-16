package com.example.newsapp

import android.content.Context

class SearchString(context: Context): SavedPreferenceString {
    private val sharedPreferences by lazy {
        context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    }
    private val key by lazy { context.resources.getString(R.string.search_string_key) }
    private val defaultValue by lazy { context.resources.getString(R.string.search_string_default) }

    private lateinit var value: String

    override fun load(): String {
        return try {
            value
        } catch (e: UninitializedPropertyAccessException) {
            value = sharedPreferences.getString(key, defaultValue)!!
            value
        }
    }

    override fun save(string: String) {
        value = string
        sharedPreferences.edit()
            .putString(key, string)
            .apply()
    }
}
