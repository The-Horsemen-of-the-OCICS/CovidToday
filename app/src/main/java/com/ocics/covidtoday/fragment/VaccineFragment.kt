package com.ocics.covidtoday.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.PlacesApi
import com.google.maps.model.PlaceDetails
import com.google.maps.model.PlaceType
import com.google.maps.model.PlacesSearchResponse
import com.google.maps.model.RankBy
import com.ocics.covidtoday.MainActivity
import com.ocics.covidtoday.R
import com.ocics.covidtoday.databinding.FragmentVaccineBinding
import com.ocics.covidtoday.util.ApiUtil
import com.ocics.covidtoday.viewmodel.MapsViewModel

const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

class VaccineFragment : Fragment(), OnMapReadyCallback {
    private val TAG = javaClass.simpleName

    private val mMapsViewModel: MapsViewModel by activityViewModels()
    private lateinit var mBinding: FragmentVaccineBinding
    private var mHandler = Handler(Looper.getMainLooper())

    // Maps
    private lateinit var mMap: GoogleMap
    private lateinit var mMapView: MapView
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentVaccineBinding.inflate(layoutInflater, container, false)
        mBinding.apply { viewmodel = mMapsViewModel }

        mMapView = mBinding.googleMap
        mMapView.onCreate(savedInstanceState)
        mMapView.getMapAsync(this)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        return mBinding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady")
        mMap = googleMap
        updateLocationUI()
        getDeviceLocation()
        mHandler.postDelayed({
            loadMarkers()
        }, 3000)
    }

    private fun updateLocationUI() {
        try {
            Log.d(TAG, "updateLocationUI, mIsLocationPermissionGranted=" + mMapsViewModel.isLocationPermissionGranted)
            if (mMapsViewModel.isLocationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                (requireActivity() as MainActivity).getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getDeviceLocation() {
        try {
            if (mMapsViewModel.isLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this.requireActivity()) { task ->
                    if (task.isSuccessful) {
                        mMapsViewModel.currentDeviceLocation = task.result
                        Log.d(TAG, mMapsViewModel.currentDeviceLocation!!.latitude.toString())

                        val curLatlng = LatLng(
                            mMapsViewModel.currentDeviceLocation!!.latitude,
                            mMapsViewModel.currentDeviceLocation!!.longitude)
                        getVaccineLocations(curLatlng)

                        mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(curLatlng,15f)
                        )
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getVaccineLocations(latlng: LatLng) {
        PlacesApi
            .nearbySearchQuery(ApiUtil.MAPS_CONTEXT, com.google.maps.model.LatLng(latlng.latitude, latlng.longitude))
            .radius(5000)
            .rankby(RankBy.PROMINENCE)
            .keyword("vaccine")
            .language("en")
            .type(PlaceType.PHARMACY)
            .setCallback(object : PendingResult.Callback<PlacesSearchResponse> {
                override fun onResult(res: PlacesSearchResponse?) {
                    if (res != null) {
                        for (result in res.results) {
                            PlacesApi
                                .placeDetails(ApiUtil.MAPS_CONTEXT, result.placeId)
                                .setCallback(object : PendingResult.Callback<PlaceDetails> {
                                    override fun onResult(details: PlaceDetails?) {
                                        Log.d(TAG, details.toString())
                                        if (details != null) {
                                            mMapsViewModel.placeDetails.add(details)
                                        }
                                    }
                                    override fun onFailure(e: Throwable?) {
                                    }
                                })
                        }
                    }
                }
                override fun onFailure(e: Throwable?) {
                }

            })
    }

    override fun onResume() {
        mMapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mMapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mMapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mMapView.onLowMemory()
        super.onLowMemory()
    }

    fun loadMarkers() {
        if (mMapsViewModel.placeDetails.size > 0) {
            mBinding.mapPlaceholder.visibility = View.GONE
            mMapView.visibility = View.VISIBLE
            for (detail in mMapsViewModel.placeDetails) {
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(
                                detail.geometry.location.lat,
                                detail.geometry.location.lng
                            )
                        )
                        .title(detail.name)
                        .snippet("Phone: ${detail.formattedPhoneNumber}")
                )
                mMapsViewModel.markers.add(marker!!)
            }
        }
    }

}