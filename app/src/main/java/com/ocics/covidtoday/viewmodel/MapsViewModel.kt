package com.ocics.covidtoday.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.Marker
import com.google.maps.model.PlaceDetails

class MapsViewModel: ViewModel() {
    // locations
    var isLocationPermissionGranted = false
    var currentDeviceLocation: Location? = null

    // places
    var placeDetails = mutableListOf<PlaceDetails>()

    // markers
    var markers = mutableListOf<Marker>()
}