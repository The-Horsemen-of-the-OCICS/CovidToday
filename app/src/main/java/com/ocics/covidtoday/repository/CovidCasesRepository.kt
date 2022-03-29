package com.ocics.covidtoday.repository

import android.content.ContentValues
import android.util.Log
import androidx.annotation.Nullable
import androidx.lifecycle.MutableLiveData
import com.ocics.covidtoday.model.CovidStatics
import com.ocics.covidtoday.model.HistoryCovidStatics
import com.ocics.covidtoday.util.CovidStaticsClient
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory


class CovidCasesRepository() {
    private val BASE_URL = "https://covid-api.mmediagroup.fr/v1/"

    private lateinit var covidStaticsClient: CovidStaticsClient

    private var covidCasesData: MutableLiveData<Map<String, Map<String, CovidStatics>>> =
        MutableLiveData<Map<String, Map<String, CovidStatics>>>()
    private var covidHistoryData: MutableLiveData<Map<String, Long>> =
        MutableLiveData<Map<String, Long>>()

    private var covidCasesConfirmedKey: ArrayList<String> = ArrayList()
    private var covidCasesConfirmedValue: ArrayList<String> = ArrayList()

    init {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
        covidStaticsClient = retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CovidStaticsClient::class.java)
    }


    public fun getCovidCases(@Nullable country: String) {
        covidStaticsClient.getCases(country)
            .enqueue(object : retrofit2.Callback<Map<String, Map<String, CovidStatics>>> {
                override fun onResponse(
                    call: Call<Map<String, Map<String, CovidStatics>>>,
                    response: Response<Map<String, Map<String, CovidStatics>>>
                ) {
                    if (response.body() !== null) {
                        covidCasesData.postValue(response.body())
                    }
                }

                override fun onFailure(
                    call: Call<Map<String, Map<String, CovidStatics>>>,
                    t: Throwable
                ) {

                }
            })
    }

    fun getCovidCasesData(): MutableLiveData<Map<String, Map<String, CovidStatics>>> {
        return covidCasesData
    }

    fun getCovidHistory(country: String, province: String) {
        covidStaticsClient.getHistory("confirmed", country)
            .enqueue(object : Callback<Map<String, HistoryCovidStatics>> {
                override fun onResponse(
                    call: Call<Map<String, HistoryCovidStatics>>,
                    response: Response<Map<String, HistoryCovidStatics>>
                ) {
                    if (response.body() != null) {
                        if (response.body()!!.get(province)?.getData() !== null) {
                            Log.e(
                                ContentValues.TAG,
                                "fillStatics: Statics : >>> " + (response.body()!!.get(province)
                                    ?.getData()?.keys?.reversed())
                            )
                            Log.e(
                                ContentValues.TAG,
                                "fillStatics: Statics : >>> " + (response.body()!!.get(province)
                                    ?.getData()?.values?.reversed())
                            )
                            val data = response.body()!![province]
                                ?.getData()
                            val dataDate = data?.keys?.reversed()

                            if (dataDate != null) {
                                covidCasesConfirmedKey.addAll(dataDate.toTypedArray())
                                val itr: Iterator<String> = dataDate.toTypedArray().iterator()
                                while (itr.hasNext()) {
                                    val key = itr.next()
                                    covidCasesConfirmedValue.add(data[key].toString())
                                }
                            }
                            Log.e(
                                ContentValues.TAG,
                                "fillStatics: covidCasesConfirmedKey : >>> $covidCasesConfirmedKey"
                            )

                            Log.e(
                                ContentValues.TAG,
                                "fillStatics: covidCasesConfirmedValue : >>> $covidCasesConfirmedValue"
                            )

                            covidHistoryData.postValue(response.body()!!.get(province)?.getData())
                        }
                    }
                }

                override fun onFailure(call: Call<Map<String, HistoryCovidStatics>>, t: Throwable) {
                    Log.e(ContentValues.TAG, "fillStatics: Statics  Failure: >>> " + t)
                }

            })
    }

    fun getCovidHistoryData(): MutableLiveData<Map<String, Long>> {
        return covidHistoryData
    }

    fun getCovidConfirmedDataKey(): ArrayList<String> {
        return covidCasesConfirmedKey
    }

    fun getCovidConfirmedDataValue(): ArrayList<String> {
        return covidCasesConfirmedValue
    }

}




