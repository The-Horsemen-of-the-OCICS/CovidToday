package com.ocics.covidtoday.util
import com.google.maps.GeoApiContext
import com.ocics.covidtoday.BuildConfig

object ApiUtil {
    var MAPS_API_KEY = BuildConfig.MAPS_API_KEY

    val MAPS_CONTEXT = GeoApiContext.Builder()
        .apiKey(MAPS_API_KEY)
        .build()
}