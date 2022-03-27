package com.ocics.covidtoday.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ocics.covidtoday.R
import com.ocics.covidtoday.databinding.FragmentCovidBinding
import com.ocics.covidtoday.model.News
import com.squareup.picasso.Picasso


class CovidFragment : Fragment() {
    private lateinit var mBinding: FragmentCovidBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentCovidBinding.inflate(layoutInflater, container, false)
        fillDataSource()
        return mBinding.root
    }

    private fun fillNewsToUI(){
        val news = News()
        val from = arrayOf("news_image", "news_title", "news_link", "news_author", "news_published_at")
        val to = intArrayOf(R.id.news_image,R.id.news_title,R.id.news_link, R.id.news_author,R.id.news_published_at)
        val adapter:SimpleAdapter = object :SimpleAdapter(
            activity,news.getNewsList(),
            R.layout.news_item,from,to
        ){
            override fun setViewImage(v: ImageView?, value: String) {
                Picasso.get().load(value).into(v)
            }
        }
        news.fetchNewsFromAPI(adapter)
        val newsList: ListView =  mBinding.root.findViewById<ListView>(R.id.covid_news_list)
        newsList.adapter = adapter
        newsList.onItemClickListener =
            OnItemClickListener { _, view, _, _ ->
            val url = (view.findViewById<View>(R.id.news_link) as TextView).text.toString()
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }
    private fun fillDataSource() {
        //Fill data sources
        fillNewsToUI()
    }

}