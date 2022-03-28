package com.ocics.covidtoday.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class HistoryCovidStatics {
    @SerializedName("country")
    @Expose(deserialize = false)
    private val country: String? = null

    @SerializedName("population")
    @Expose(deserialize = false)
    private val population: Long? = null

    @SerializedName("sq_km_area")
    @Expose(deserialize = false)
    private val area: Long? = null

    @SerializedName("life_expectancy")
    @Expose(deserialize = false)
    private val lifeExpectancy: String? = null

    @SerializedName("elevation_in_meters")
    @Expose(deserialize = false)
    private val elevation: String? = null

    @SerializedName("continent")
    @Expose(deserialize = false)
    private val continent: String? = null

    @SerializedName("abbreviation")
    @Expose(deserialize = false)
    private val abbreviation: String? = null

    @SerializedName("location")
    @Expose(deserialize = false)
    private val location: String? = null

    @SerializedName("iso")
    @Expose(deserialize = false)
    private val iso: Long? = null

    @SerializedName("capital_city")
    @Expose(deserialize = false)
    private val capitalCity: String? = null

    @SerializedName("dates")
    @Expose
    private val data: Map<String?, Long?>? = null

    fun getData(): Map<String?, Long?>? {
        return data
    }
}