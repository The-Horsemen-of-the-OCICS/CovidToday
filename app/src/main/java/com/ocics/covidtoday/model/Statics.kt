package com.ocics.covidtoday.model

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.ocics.covidtoday.R
import com.ocics.covidtoday.util.CovidStaticsClient
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory

class Statics(val country: String, val province: String, val aaChartView: AAChartView) {
    private val BASE_URL = "https://covid-api.mmediagroup.fr/v1/"
    private lateinit var covidStaticsClient: CovidStaticsClient

    private var confirmedDataMap: Map<String, Long> = mapOf()
    private var deathDataMap: Map<String, Long> = mapOf()
    private var recoveredDataMap: Map<String, Long> = mapOf()

    init {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
        covidStaticsClient = retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CovidStaticsClient::class.java)
        fetchConfirmedDataFromAPI()
        fetchDeathDataFromAPI()
        fetchRecoveredDataFromAPI()
        fetchData()
    }

    public fun getConfirmedDataMap(): Map<String, Long> {
        Log.d(TAG, "getConfirmedDataMap: xxxx$confirmedDataMap")
        return confirmedDataMap
    }

    public fun getDeathDataMap(): Map<String, Long> {
        return deathDataMap
    }

    public fun getRecoveredDataMap(): Map<String, Long> {
        return recoveredDataMap
    }

    fun fetchConfirmedDataFromAPI() {
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
                        }
                    }
                }

                override fun onFailure(call: Call<Map<String, HistoryCovidStatics>>, t: Throwable) {
                    Log.e(ContentValues.TAG, "fetchConfirmedDataFromAPI Failure: >>> $t")
                }
            })

    }

    fun fetchDeathDataFromAPI() {
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
                        }
                    }
                }

                override fun onFailure(call: Call<Map<String, HistoryCovidStatics>>, t: Throwable) {
                    Log.e(ContentValues.TAG, "fetchConfirmedDataFromAPI Failure: >>> $t")
                }
            })
    }

    fun fetchRecoveredDataFromAPI() {
        covidStaticsClient.getHistory("recovered", country)
            .enqueue(object : Callback<Map<String, HistoryCovidStatics>> {
                override fun onResponse(
                    call: Call<Map<String, HistoryCovidStatics>>,
                    response: Response<Map<String, HistoryCovidStatics>>
                ) {
                    if (response.body() != null) {

                        if (response.body()!![province]?.getData() !== null) {
                            recoveredDataMap = response.body()!![province]?.getData()!!
                            Log.d(TAG, "onResponse: recovered >>$recoveredDataMap")
                        }
                    }
                }

                override fun onFailure(call: Call<Map<String, HistoryCovidStatics>>, t: Throwable) {
                    Log.e(ContentValues.TAG, "fetchConfirmedDataFromAPI Failure: >>> $t")
                }
            })
    }

    fun fetchData() {
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
                            covidStaticsClient.getHistory("deaths", country)
                                .enqueue(object : Callback<Map<String, HistoryCovidStatics>> {
                                    override fun onResponse(
                                        call: Call<Map<String, HistoryCovidStatics>>,
                                        response: Response<Map<String, HistoryCovidStatics>>
                                    ) {
                                        if (response.body() != null) {
                                            if (response.body()!![province]?.getData() !== null) {
                                                deathDataMap =
                                                    response.body()!![province]?.getData()!!
                                                Log.d(TAG, "onResponse: deaths >>$deathDataMap")
                                                covidStaticsClient.getHistory("recovered", country)
                                                    .enqueue(object :
                                                        Callback<Map<String, HistoryCovidStatics>> {
                                                        override fun onResponse(
                                                            call: Call<Map<String, HistoryCovidStatics>>,
                                                            response: Response<Map<String, HistoryCovidStatics>>
                                                        ) {
                                                            if (response.body() != null) {

                                                                if (response.body()!![province]?.getData() !== null) {
                                                                    recoveredDataMap =
                                                                        response.body()!![province]?.getData()!!
                                                                    Log.d(
                                                                        TAG,
                                                                        "onResponse: recovered >>$recoveredDataMap"
                                                                    )

                                                                    drawChart()
                                                                }
                                                            }
                                                        }

                                                        override fun onFailure(
                                                            call: Call<Map<String, HistoryCovidStatics>>,
                                                            t: Throwable
                                                        ) {
                                                            Log.e(
                                                                ContentValues.TAG,
                                                                "fetchConfirmedDataFromAPI Failure: >>> $t"
                                                            )
                                                        }
                                                    })
                                            }
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<Map<String, HistoryCovidStatics>>,
                                        t: Throwable
                                    ) {
                                        Log.e(
                                            ContentValues.TAG,
                                            "fetchConfirmedDataFromAPI Failure: >>> $t"
                                        )
                                    }
                                })
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
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Tokyo")
                        .data(arrayOf(7.0, 6.9)),

                    )
            )
        //The chart view object calls the instance object of AAChartModel and draws the final graphic
        //The chart view object calls the instance object of AAChartModel and draws the final graphic
        aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }

}