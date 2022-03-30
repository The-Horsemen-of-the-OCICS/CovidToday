package com.ocics.covidtoday.fragment

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.ocics.covidtoday.MainActivity
import com.ocics.covidtoday.R
import com.ocics.covidtoday.databinding.FragmentCovidBinding
import com.ocics.covidtoday.model.News
import com.ocics.covidtoday.model.Statics
import com.ocics.covidtoday.repository.CovidCasesRepository
import com.squareup.picasso.Picasso
import java.util.*


class CovidFragment : Fragment() {
    private lateinit var mBinding: FragmentCovidBinding
    private val mCovidCasesRepo: CovidCasesRepository = CovidCasesRepository()
    private lateinit var viewModel: CovidStatsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[CovidStatsViewModel::class.java]
        viewModel.initialize()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentCovidBinding.inflate(layoutInflater, container, false)
        fillDataSource()
        return mBinding.root
    }

    private fun fillNewsToUI() {
        val currentCountry = (activity as MainActivity).country
        val currentProvince = (activity as MainActivity).province

        val news = News(currentCountry, currentProvince)
        val from =
            arrayOf("news_image", "news_title", "news_link", "news_author", "news_published_at")
        val to = intArrayOf(
            R.id.news_image,
            R.id.news_title,
            R.id.news_link,
            R.id.news_author,
            R.id.news_published_at
        )
        val adapter: SimpleAdapter = object : SimpleAdapter(
            activity, news.getNewsList(),
            R.layout.news_item, from, to
        ) {
            override fun setViewImage(v: ImageView?, value: String) {
                Picasso.get().load(value).into(v)
            }
        }
        news.fetchNewsFromAPI(adapter)
        val newsList: ListView = mBinding.root.findViewById<ListView>(R.id.covid_news_list)
        newsList.adapter = adapter
        newsList.onItemClickListener =
            OnItemClickListener { _, view, _, _ ->
                val url = (view.findViewById<View>(R.id.news_link) as TextView).text.toString()
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
    }

    private fun fillStaticsToUI() {
        val currentCountry = (activity as MainActivity).country
        val currentProvince = (activity as MainActivity).province

        val statics = Statics(currentCountry, currentProvince)

        Log.d(TAG, "fillStaticsToUI: statics" + statics.getConfirmedDataMap())


        val aaChartView = mBinding.root.findViewById<AAChartView>(R.id.aa_chart_view)
        val aaChartModel: AAChartModel = AAChartModel()
            .chartType(AAChartType.Area)
            .title("Statics in Ontario")
            .subtitle("Cases")
            .backgroundColor(R.color.light_gray)
            .dataLabelsEnabled(true)
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Tokyo")
                        .data(arrayOf(7.0, 6.9)),

                    )
            )
        //The chart view object calls the instance object of AAChartModel and draws the final graphic
        aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }



    fun fillDataSource() {
        //Fill data sources
        fillNewsToUI()
        fillStaticsToUI()
    }

}