package com.example.newsapp

class DataSet(private val recyclerAdapter: RecyclerAdapter) {
    private var contents: ArrayList<Model.Article?> = ArrayList()
    private var pagesLoaded = 0
    private var totalArticles = 0

    fun init(result: Model.Result) {
        contents = ArrayList(result.articles)
        totalArticles = result.totalResults
        pagesLoaded = 1
        recyclerAdapter.setContents(contents)
        recyclerAdapter.notifyDataSetChanged()
    }

    fun add(result: Model.Result) {
        ensureNoNullElements()
        totalArticles = result.totalResults
        contents.addAll(result.articles)
        pagesLoaded++
        recyclerAdapter.notifyDataSetChanged()
    }

    fun addProgressBar() {
        contents.add(null)
        recyclerAdapter.notifyDataSetChanged()
    }

    fun removeProgressBar() {
        ensureNoNullElements()
        recyclerAdapter.notifyDataSetChanged()
    }

    fun isFullyLoaded(): Boolean {
        return totalArticles <= contents.size
    }

    fun size() = contents.size

    fun nextPage(): Int = pagesLoaded + 1

    private fun ensureNoNullElements() {
        if (contents.isNotEmpty() && contents[contents.size - 1] == null) {
            contents.removeAt(contents.size - 1)
        }
    }
}