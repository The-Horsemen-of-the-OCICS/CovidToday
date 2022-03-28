package com.ocics.covidtoday.util

import com.ocics.covidtoday.model.CovidStatics
import com.ocics.covidtoday.model.HistoryCovidStatics
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CovidStaticsClient {
    @GET("cases")
    fun getCases(
        @Query("country") country: String?
    ): Call<Map<String?, Map<String?, CovidStatics?>?>?>?

    @GET("history")
    fun getHistory(
        @Query("status") status: String?,
        @Query("country") country: String?
    ): Call<Map<String?, HistoryCovidStatics?>?>?
}