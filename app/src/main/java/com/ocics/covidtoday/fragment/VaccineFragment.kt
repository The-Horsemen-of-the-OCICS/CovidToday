package com.ocics.covidtoday.fragment

import android.content.ClipData
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.FileProvider
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
import com.ocics.covidtoday.model.VaccineStatistic
import com.ocics.covidtoday.util.ApiUtil
import com.ocics.covidtoday.util.VaccineStatClient
import com.ocics.covidtoday.viewmodel.MapsViewModel
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

class VaccineFragment : Fragment(), OnMapReadyCallback, PlacesRecyclerAdapter.ClickListener {
    private val TAG = javaClass.simpleName

    private val mMapsViewModel: MapsViewModel by activityViewModels()
    private lateinit var mBinding: FragmentVaccineBinding
    private var mHandler = Handler(Looper.getMainLooper())

    // Vaccine Statics
    private val BASE_URL = "https://covid-api.mmediagroup.fr/v1/"
    private lateinit var vaccineStatClient: VaccineStatClient
    private lateinit var administeredValueTextView: TextView
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

        administeredValueTextView = mBinding.administeredValue
        fullyVaccinatedValueTextView = mBinding.fullyVaccinatedValue
        partiallyVaccinatedValueTextView = mBinding.partiallyVaccinatedValue

        fillVaccineStatisticsToUI()

        mBinding.shareButtonVaccine.setOnClickListener {
            share(getBitmapFromView(mBinding.vaccineStatCard))
        }
        mBinding.shareButtonVaccine.visibility = View.INVISIBLE

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

    fun fillVaccineStatisticsToUI() {
        val currentCountry = (activity as MainActivity).country

        val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
        vaccineStatClient = retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VaccineStatClient::class.java)
        fetchVaccineDataFromAPI(currentCountry)

    }

    private fun fetchVaccineDataFromAPI(country: String) {
        vaccineStatClient.getVaccines(country)
            .enqueue(object : Callback<Map<String, VaccineStatistic>> {
                override fun onResponse(
                    call: Call<Map<String, VaccineStatistic>>,
                    response: Response<Map<String, VaccineStatistic>>
                ) {
                    if (response.body() != null) {
                        mBinding.vaccineCardTitle.text = "Vaccine statistics (${(activity as MainActivity).country})"
                        administeredValueTextView.text =
                            response.body()!!["All"]?.getAdministered().toString()
                        fullyVaccinatedValueTextView.text =
                            response.body()!!["All"]?.getPeopleFullyVaccinated().toString()
                        partiallyVaccinatedValueTextView.text =
                            response.body()!!["All"]?.getPeoplePartiallyVaccinated().toString()
                        mBinding.shareButtonVaccine.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<Map<String, VaccineStatistic>>, t: Throwable) {
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

    private fun getVaccineStaticsData(): String {
        return "\n Administered: ${administeredValueTextView.text} \n Fully Vaccinated: ${fullyVaccinatedValueTextView.text} \n  Partially Vaccinated: ${partiallyVaccinatedValueTextView.text}"
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        // Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null)
            bgDrawable.draw(canvas) else
            canvas.drawColor(Color.WHITE)
        // Draw the view on the canvas
        view.draw(canvas)

        return returnedBitmap
    }

    private fun share(bitmap: Bitmap?) {
        if (bitmap != null) {
            val mediaStorageDir = File(context?.externalCacheDir.toString() + "Image.png")
            try {
                val outputStream = FileOutputStream(java.lang.String.valueOf(mediaStorageDir))
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val imageUri = FileProvider.getUriForFile(
                requireActivity(),
                requireActivity().applicationContext.packageName + ".provider",
                mediaStorageDir
            )
            val mimeType = arrayOf("image/png")
            if (imageUri != null) {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    type = "image/*"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    clipData = ClipData(
                        "Vaccine Stats",
                        mimeType,
                        ClipData.Item(imageUri)
                    )
                }

                startActivity(Intent.createChooser(sendIntent, "Share with"))
            }

        }
    }
}
