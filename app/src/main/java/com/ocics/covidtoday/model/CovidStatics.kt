package com.ocics.covidtoday.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

public class CovidStatics {
    @SerializedName("confirmed")
    @Expose
    private val confirmed: Long? = null

    @SerializedName("recovered")
    @Expose
    private val recovered: Long? = null

    @SerializedName("deaths")
    @Expose
    private val deaths: Long? = null

    @SerializedName("country")
    @Expose
    private val country: String? = null

    @SerializedName("population")
    @Expose
    private val population: Long? = null

    @SerializedName("sq_km_area")
    @Expose(deserialize = false)
    private val area: Float? = null

    @SerializedName("life_expectancy")
    @Expose
    private val lifeExpectancy: String? = null

    @SerializedName("elevation_in_meters")
    @Expose
    private val elevation: String? = null

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
    private val latitude: String? = null

    @SerializedName("long")
    @Expose
    private val longitude: String? = null

    @SerializedName("updated")
    @Expose
    private val timeStamp: String? = null

    fun getConfirmed(): Long? {
        return confirmed
    }

    fun getRecovered(): Long? {
        return recovered
    }

    fun getDeaths(): Long? {
        return deaths
    }

    fun getCountry(): String? {
        return country
    }

    fun getPopulation(): Long? {
        return population
    }

    fun getArea(): Float? {
        return area
    }

    fun getLifeExpectancy(): String? {
        return lifeExpectancy
    }

    fun getElevation(): String? {
        return elevation
    }

    fun getContinent(): String? {
        return continent
    }

    fun getAbbreviation(): String? {
        return abbreviation
    }

    fun getLocation(): String? {
        return location
    }

    fun getIso(): Long? {
        return iso
    }

    fun getCapitalCity(): String? {
        return capitalCity
    }

    fun getLatitude(): String? {
        return latitude
    }

    fun getLongitude(): String? {
        return longitude
    }

    fun getTimeStamp(): String? {
        return timeStamp
    }
}