package com.example.newsapp.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.models.NewsApiResponse
import com.example.newsapp.R
import com.example.newsapp.Utils.Companion.formatDateTime
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.news_list_item.view.listDateView
import kotlinx.android.synthetic.main.news_list_item.view.listImageView
import kotlinx.android.synthetic.main.news_list_item.view.listTitleView

class MainAdapter (
    private val clickListener : (NewsApiResponse.Article) -> Unit,
    private val lastItemListener : () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items : ArrayList<NewsApiResponse.Article?> = arrayListOf(null)
    private var isLoading: Boolean = false
    private var isFull: Boolean = false
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    fun setItems(items : List<NewsApiResponse.Article?>, isFull: Boolean) {
        println("setting items. now have ${items.size} items")
        this.isFull = isFull
        if (!isFull) this.items.add(null)
        this.items = ArrayList(items)
        isLoading = false
        notifyDataSetChanged()
    }

    fun finishLoading() {
        if (items.isNotEmpty() && items[items.size - 1] == null) {
            items.removeAt(items.size - 1)
            notifyDataSetChanged()
            isFull = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.news_list_item, parent, false)
            return ItemViewHolder(view)
        }
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.loading_item, parent, false)
        return LoadingViewHolder(view)
     }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        println("binding article ${position}")
        if (holder is ItemViewHolder) {
            holder.bind(items[position], clickListener)
        }
        if (position >= items.size - 2 && !isLoading && !isFull) {
            println("Main adapter binded last item of ${items.size}. adding more")
            isLoading = true
            lastItemListener()
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        if (items[position] == null) {
            return VIEW_TYPE_LOADING
        }
        return VIEW_TYPE_ITEM
    }

    class ItemViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        fun bind(
            article: NewsApiResponse.Article?,
            clickListener: (NewsApiResponse.Article) -> Unit
        ) {
            if (article == null) return
            val imageWidth = itemView.context.resources.getDimensionPixelSize(R.dimen.image_width)
            try {
                Picasso.with(itemView.context)
                    .load(article.urlToImage)
                    .resize(imageWidth, 0)
                    .error(R.drawable.no_image_available)
                    .placeholder(R.drawable.no_image_available)
                    .into(itemView.listImageView)
            } catch (e: Exception) {
                itemView.listImageView.setImageResource(R.drawable.no_image_available)
            }

            itemView.listTitleView.text = article.title
            itemView.listDateView.text = formatDateTime(article.publishedAt)
            itemView.setOnClickListener { clickListener(article) }
        }
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
