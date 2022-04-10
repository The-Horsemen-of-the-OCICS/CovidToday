package com.ocics.covidtoday.util

import com.ocics.covidtoday.model.CovidStatistic
import com.ocics.covidtoday.model.HistoryCovidStatistic
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CovidStatClient {
    @GET("cases")
    fun getCases(
        @Query("country") country: String
    ): Call<Map<String, Map<String, CovidStatistic>>>

    @GET("history")
    fun getHistory(
        @Query("status") status: String,
        @Query("country") country: String
    ): Call<Map<String, HistoryCovidStatistic>>
}