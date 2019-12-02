package com.example.newsapp.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.models.NewsApiResponse
import com.example.newsapp.R
import com.example.newsapp.Utils.Companion.formatDateTime
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.news_list_item.view.*

class RecyclerAdapter (
    private val listener : Listener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items : List<NewsApiResponse.Article?> = ArrayList()
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    interface Listener {
        fun onItemClick(article : NewsApiResponse.Article)
    }

    fun setContents(items : List<NewsApiResponse.Article?>) {
        this.items = items
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
        if (holder is ItemViewHolder) {
            holder.bind(items[position], listener)
        }
    }

    override fun getItemCount(): Int = items.count()

    override fun getItemViewType(position: Int): Int {
        if (items[position] == null)
            return VIEW_TYPE_LOADING
        return VIEW_TYPE_ITEM
    }

    class ItemViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        fun bind(article: NewsApiResponse.Article?, listener: Listener) {
            if (article == null)
                return
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
            itemView.setOnClickListener{ listener.onItemClick(article) }
        }
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}