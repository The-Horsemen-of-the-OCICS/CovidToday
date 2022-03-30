package com.ocics.covidtoday.fragment

import android.content.ContentValues
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
import com.ocics.covidtoday.model.HistoryCovidStatics
import com.ocics.covidtoday.model.News
import com.ocics.covidtoday.util.CovidStaticsClient
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory


class CovidFragment : Fragment() {
    private lateinit var mBinding: FragmentCovidBinding

    private val BASE_URL = "https://covid-api.mmediagroup.fr/v1/"
    private lateinit var covidStaticsClient: CovidStaticsClient
    private var confirmedDataMap: Map<String, Long> = mapOf()
    private var deathDataMap: Map<String, Long> = mapOf()
    private lateinit var aaChartView: AAChartView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    fun fillDataSource() {
        //Fill data sources
        fillNewsToUI()
        fillStaticsToUI()
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

        aaChartView = mBinding.root.findViewById<AAChartView>(R.id.aa_chart_view)

        val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
        covidStaticsClient = retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CovidStaticsClient::class.java)
        fetchConfirmedDataFromAPI(currentCountry, currentProvince)

    }


    fun fetchConfirmedDataFromAPI(country: String, province: String) {
        covidStaticsClient.getHistory("confirmed", country)
            .enqueue(object : Callback<Map<String, HistoryCovidStatics>> {
                override fun onResponse(
                    call: Call<Map<String, HistoryCovidStatics>>,
                    response: Response<Map<String, HistoryCovidStatics>>
                ) {
                    if (response.body() != null) {
                        if (response.body()!![province]?.getData() !== null) {
                            confirmedDataMap = response.body()!![province]?.getData()!!
                            Log.d(TAG, "onResponse: confirmed >>$confirmedDataMap")
                            fetchDeathDataFromAPI(country, province)
                        }
                    }
                }

                override fun onFailure(call: Call<Map<String, HistoryCovidStatics>>, t: Throwable) {
                    Log.e(ContentValues.TAG, "fetchConfirmedDataFromAPI Failure: >>> $t")
                }
            })
    }

    fun fetchDeathDataFromAPI(country: String, province: String) {
        covidStaticsClient.getHistory("deaths", country)
            .enqueue(object : Callback<Map<String, HistoryCovidStatics>> {
                override fun onResponse(
                    call: Call<Map<String, HistoryCovidStatics>>,
                    response: Response<Map<String, HistoryCovidStatics>>
                ) {
                    if (response.body() != null) {
                        if (response.body()!![province]?.getData() !== null) {
                            deathDataMap = response.body()!![province]?.getData()!!
                            Log.d(TAG, "onResponse: deaths >>$deathDataMap")
                            drawChart()
                        }
                    }
                }

                override fun onFailure(call: Call<Map<String, HistoryCovidStatics>>, t: Throwable) {
                    Log.e(ContentValues.TAG, "fetchConfirmedDataFromAPI Failure: >>> $t")
                }
            })
    }


    fun drawChart() {
        val aaChartModel: AAChartModel = AAChartModel()
            .chartType(AAChartType.Area)
            .title("Statics in Ontario")
            .subtitle("Cases")
            .backgroundColor(R.color.light_gray)
            .dataLabelsEnabled(true)
            .yAxisTitle("")
            .categories(confirmedDataMap.keys.reversed().toTypedArray())
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Confirmed")
                        .data(confirmedDataMap.values.reversed().toTypedArray()),
                    AASeriesElement()
                        .name("Deaths")
                        .data(
                            deathDataMap.values.reversed().toTypedArray()
                        ),
                ),

                )
        //The chart view object calls the instance object of AAChartModel and draws the final graphic
        aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }
}