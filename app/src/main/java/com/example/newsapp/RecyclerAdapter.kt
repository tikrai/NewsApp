package com.example.newsapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.MainActivity.Companion.dp
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.news_list_item.view.*

class RecyclerAdapter (
    private val items : List<Model.Article>,
    private val listener : Listener
) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    interface Listener {
        fun onItemClick(article : Model.Article)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], listener)
    }

    override fun getItemCount(): Int = items.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_list_item, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        fun bind(article: Model.Article, listener: Listener) {
            try {
                Picasso.with(itemView.context)
                    .load(article.urlToImage)
                    .resize(dp(150), 0)
                    .error(R.drawable.no_image_available)
                    .placeholder(R.drawable.no_image_available)
                    .into(itemView.listImageView)
            } catch (e: Exception) {
                itemView.listImageView.setImageResource(R.drawable.no_image_available)
            }

            itemView.listTitleView.text = article.title
            itemView.listDateView.text = article.publishedAt
            itemView.setOnClickListener{ listener.onItemClick(article) }

// TODO check maybe glide can do this better

//            val requestOptions = RequestOptions()
//                .placeholder(R.drawable.no_image_available)
//                .override(150, 0)
//                .error(R.drawable.no_image_available)
//            Glide.with(itemView.context)
//                .applyDefaultRequestOptions(requestOptions)
//                .load(article.urlToImage)
//                .into(itemView.listImageView)
        }
    }
}