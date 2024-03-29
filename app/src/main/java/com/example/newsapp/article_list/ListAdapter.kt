package com.example.newsapp.article_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.models.NewsApiResponse.Article
import com.example.newsapp.R
import com.example.newsapp.Utils.formatDateTime
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.news_list_item.view.listDateView
import kotlinx.android.synthetic.main.news_list_item.view.listImageView
import kotlinx.android.synthetic.main.news_list_item.view.listTitleView

class ListAdapter (
    private val clickListener : (Article) -> Unit,
    private val onLastItemShownListener : () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items : ArrayList<Article?> = arrayListOf(null)
    private var isLoading: Boolean = false
    private var loadingIsFinished: Boolean = false
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    fun setItems(items : List<Article?>, loadingIsFinished: Boolean) {
        this.items = ArrayList(items)
        this.loadingIsFinished = loadingIsFinished
        if (!loadingIsFinished) {
            this.items.add(null)
        }
        isLoading = false
        notifyDataSetChanged()
    }

    fun setLoadingIsFinished() {
        if (items.isNotEmpty() && items[items.size - 1] == null) {
            items.removeAt(items.size - 1)
            notifyDataSetChanged()
            loadingIsFinished = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.news_list_item, parent, false)
            ItemViewHolder(view)
        } else {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.loading_item, parent, false)
            LoadingViewHolder(view)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            holder.bind(items[position], clickListener)
            if (position >= items.size - 2 && !isLoading && !loadingIsFinished) {
                isLoading = true
                onLastItemShownListener()
            }
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
            article: Article?,
            clickListener: (Article) -> Unit
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
