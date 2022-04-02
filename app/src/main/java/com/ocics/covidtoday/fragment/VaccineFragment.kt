package com.ocics.covidtoday.fragment

import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.PendingResult
import com.google.maps.PlacesApi
import com.google.maps.model.PlaceDetails
import com.google.maps.model.PlaceType
import com.google.maps.model.PlacesSearchResponse
import com.google.maps.model.RankBy
import com.ocics.covidtoday.MainActivity
import com.ocics.covidtoday.R
import com.ocics.covidtoday.adapter.PlacesRecyclerAdapter
import com.ocics.covidtoday.databinding.FragmentVaccineBinding
import com.ocics.covidtoday.model.VaccineStatics
import com.ocics.covidtoday.util.ApiUtil
import com.ocics.covidtoday.util.VaccineStaticsClient
import com.ocics.covidtoday.viewmodel.MapsViewModel
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory

const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

class VaccineFragment : Fragment(), OnMapReadyCallback, PlacesRecyclerAdapter.ClickListener {
    private val TAG = javaClass.simpleName

    private val mMapsViewModel: MapsViewModel by activityViewModels()
    private lateinit var mBinding: FragmentVaccineBinding
    private var mHandler = Handler(Looper.getMainLooper())

    // Vaccine Statics
    private val BASE_URL = "https://covid-api.mmediagroup.fr/v1/"
    private lateinit var vaccineStaticsClient: VaccineStaticsClient
    private lateinit var vaccinatedValueTextView: TextView
    private lateinit var fullyVaccinatedValueTextView: TextView
    private lateinit var partiallyVaccinatedValueTextView: TextView

    // Maps
    private lateinit var mMap: GoogleMap
    private lateinit var mMapView: MapView
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    // Recyclerview
    private lateinit var mPlacesRecyclerAdapter: PlacesRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentVaccineBinding.inflate(layoutInflater, container, false)
        mBinding.apply { viewmodel = mMapsViewModel }

        mMapView = mBinding.googleMap
        mMapView.onCreate(savedInstanceState)
        mMapView.getMapAsync(this)
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())

        mPlacesRecyclerAdapter = PlacesRecyclerAdapter()
        mPlacesRecyclerAdapter.setClickListener(this)

        mBinding.placesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mBinding.placesRecyclerView.adapter = mPlacesRecyclerAdapter

        vaccinatedValueTextView = mBinding.overallVaccinatedValue
        fullyVaccinatedValueTextView = mBinding.fullyVaccinatedValue
        partiallyVaccinatedValueTextView = mBinding.partiallyVaccinatedValue

        fillVaccineStaticsToUI()

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
            Log.d(
                TAG,
                "updateLocationUI, mIsLocationPermissionGranted=" + mMapsViewModel.isLocationPermissionGranted
            )
            if (mMapsViewModel.isLocationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style
                    )
                )
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
                            mMapsViewModel.currentDeviceLocation!!.longitude
                        )
                        getVaccineLocations(curLatlng)

                        mMap.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(curLatlng, 15f)
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
            .nearbySearchQuery(
                ApiUtil.MAPS_CONTEXT,
                com.google.maps.model.LatLng(latlng.latitude, latlng.longitude)
            )
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

    private fun fillVaccineStaticsToUI() {
        val currentCountry = (activity as MainActivity).country

        val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
        vaccineStaticsClient = retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VaccineStaticsClient::class.java)
        fetchVaccineDataFromAPI(currentCountry)

    }

    private fun fetchVaccineDataFromAPI(country: String) {
        vaccineStaticsClient.getVaccines(country)
            .enqueue(object : Callback<Map<String, VaccineStatics>> {
                override fun onResponse(
                    call: Call<Map<String, VaccineStatics>>,
                    response: Response<Map<String, VaccineStatics>>
                ) {
                    if (response.body() != null) {
                        vaccinatedValueTextView.text =
                            response.body()!!["All"]?.getPeopleVaccinated().toString()
                        fullyVaccinatedValueTextView.text =
                            response.body()!!["All"]?.getPeopleFullyVaccinated().toString()
                        partiallyVaccinatedValueTextView.text =
                            response.body()!!["All"]?.getPeoplePartiallyVaccinated().toString()
                    }
                }

                override fun onFailure(call: Call<Map<String, VaccineStatics>>, t: Throwable) {
                    Log.e(ContentValues.TAG, "fetchConfirmedDataFromAPI Failure: >>> $t")
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
            mPlacesRecyclerAdapter.mPlacesList = ArrayList(mMapsViewModel.placeDetails)
            mPlacesRecyclerAdapter.notifyDataSetChanged()
        }
    }

    override fun onClickCallback(index: Int) {
        Log.d(TAG, "onClickCallback: $index")
        if (index != -1) {
            mMapsViewModel.markers[index].showInfoWindow()
            mMap.moveCamera(
                CameraUpdateFactory
                    .newLatLngZoom(mMapsViewModel.markers[index].position, 15f)
            )
        }
    }

}