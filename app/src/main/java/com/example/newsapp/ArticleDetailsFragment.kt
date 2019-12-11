package com.example.newsapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.newsapp.models.NewsApiResponse.Article
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_article_details.view.articleToolbar
import kotlinx.android.synthetic.main.fragment_article_details.view.authorView
import kotlinx.android.synthetic.main.fragment_article_details.view.button
import kotlinx.android.synthetic.main.fragment_article_details.view.dateView
import kotlinx.android.synthetic.main.fragment_article_details.view.descriptionView
import kotlinx.android.synthetic.main.fragment_article_details.view.imageView
import kotlinx.android.synthetic.main.fragment_article_details.view.titleView

class ArticleDetailsFragment : Fragment() {

    companion object {

        private const val ARTICLE_MODEL = "model"

        fun newInstance(article: Article): ArticleDetailsFragment {
            val args = Bundle()
            args.putSerializable(ARTICLE_MODEL, article)

            val fragment = ArticleDetailsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val article = arguments!!.getSerializable(ARTICLE_MODEL) as Article
        val view: View = inflater.inflate(R.layout.fragment_article_details, container, false)

        view.articleToolbar.setNavigationOnClickListener {
            activity!!.onBackPressed()
        }
        try {
            Picasso.with(context)
                .load(article.urlToImage ?: "")
                .error(R.drawable.no_image_available)
                .into(view.imageView)
        } catch (e: Exception) {
            view.imageView.setBackgroundResource(R.drawable.no_image_available)
        }
        view.titleView.text = article.title
        view.descriptionView.text = article.description
        view.authorView.text = article.author
        view.dateView.text = Utils.formatDateTime(article.publishedAt)
        view.button.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)))
        }
        return view
    }
}
