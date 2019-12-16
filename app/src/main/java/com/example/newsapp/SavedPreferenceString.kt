package com.example.newsapp

interface SavedPreferenceString {
    fun save(string: String)
    fun load(): String
}
