package com.ocics.covidtoday.fragment

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.ocics.covidtoday.repository.CovidCasesRepository
import com.squareup.picasso.Picasso


class CovidFragment : Fragment() {
    private lateinit var mBinding: FragmentCovidBinding
    private val mCovidCasesRepo: CovidCasesRepository = CovidCasesRepository()
    private lateinit var viewModel: CovidStatsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[CovidStatsViewModel::class.java]
        viewModel.initialize()
//        viewModel.getResponseData().observe(this) { countries ->
//            if (countries != null) {
//                viewModel.setCountriesAndProvinces()
//            }
//        }

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

    private fun fillStatics() {
        val aaChartView = mBinding.root.findViewById<AAChartView>(R.id.aa_chart_view)


        mCovidCasesRepo.getCovidHistoryCases("Canada", "Ontario")

        val historyCovidStatics = viewModel.getHistoryStatsData()
        Log.i(TAG, "fillStatics: History Statics : >>> ${historyCovidStatics.toString()}")

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
                        .data(
                            arrayOf(
                                7.0,
                                6.9,
                                9.5,
                                14.5,
                                18.2,
                                21.5,
                                25.2,
                                26.5,
                                23.3,
                                18.3,
                                13.9,
                                9.6
                            )
                        ),
                    AASeriesElement()
                        .name("NewYork")
                        .data(
                            arrayOf(
                                0.2,
                                0.8,
                                5.7,
                                11.3,
                                17.0,
                                22.0,
                                24.8,
                                24.1,
                                20.1,
                                14.1,
                                8.6,
                                2.5
                            )
                        ),
                    AASeriesElement()
                        .name("London")
                        .data(
                            arrayOf(
                                0.9,
                                0.6,
                                3.5,
                                8.4,
                                13.5,
                                17.0,
                                18.6,
                                17.9,
                                14.3,
                                9.0,
                                3.9,
                                1.0
                            )
                        ),
                    AASeriesElement()
                        .name("Berlin")
                        .data(
                            arrayOf(
                                3.9,
                                4.2,
                                5.7,
                                8.5,
                                11.9,
                                15.2,
                                17.0,
                                16.6,
                                14.2,
                                10.3,
                                6.6,
                                4.8
                            )
                        )
                )
            )
        //The chart view object calls the instance object of AAChartModel and draws the final graphic
        aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }

    fun fillDataSource() {
        //Fill data sources
        fillNewsToUI()
        fillStatics()
    }

}