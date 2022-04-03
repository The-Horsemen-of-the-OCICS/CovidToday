package com.ocics.covidtoday.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class VaccineStatics {
    @SerializedName("administered")
    @Expose
    private val administered: Long? = null

    @SerializedName("people_vaccinated")
    @Expose
    private val peopleVaccinated: Long? = null

    @SerializedName("people_partially_vaccinated")
    @Expose
    private val peoplePartiallyVaccinated: Long? = null

    @SerializedName("country")
    @Expose
    private val country: String? = null

    @SerializedName("population")
    @Expose
    private val population: Long? = null

    @SerializedName("sq_km_area")
    @Expose
    private val sqKmArea: Long? = null

    @SerializedName("life_expectancy")
    @Expose
    private val lifeExpectancy: String? = null

    @SerializedName("elevation_in_meters")
    @Expose
    private val elevationInMeters: String? = null

    @SerializedName("continent")
    @Expose
    private val continent: String? = null

    @SerializedName("abbreviation")
    @Expose
    private val abbreviation: String? = null

    @SerializedName("location")
    @Expose
    private val location: String? = null

    @SerializedName("iso")
    @Expose
    private val iso: Long? = null

    @SerializedName("capital_city")
    @Expose
    private val capitalCity: String? = null

    @SerializedName("lat")
    @Expose
    private val lat: String? = null

    @SerializedName("long")
    @Expose
    private val long: String? = null

    @SerializedName("updated")
    @Expose
    private val updated: String? = null

    fun getAdministered(): Long? {
        return administered
    }

    fun getPeopleFullyVaccinated(): Long? {
        return peopleVaccinated
    }

    fun getPeoplePartiallyVaccinated(): Long? {
        return peoplePartiallyVaccinated
    }


}