package com.ocics.covidtoday.util

import com.ocics.covidtoday.model.VaccineStatistic
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query



interface VaccineStatClient {
    @GET("vaccines")
    fun getVaccines(
        @Query("country") country: String
    ): Call<Map<String, VaccineStatistic>>
}