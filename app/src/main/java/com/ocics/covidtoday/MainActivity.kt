package com.ocics.covidtoday

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ocics.covidtoday.databinding.ActivityMainBinding
import com.ocics.covidtoday.fragment.CovidFragment
import com.ocics.covidtoday.fragment.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.ocics.covidtoday.fragment.VaccineFragment
import com.ocics.covidtoday.model.Region
import com.ocics.covidtoday.util.getJsonDataFromAsset
import com.ocics.covidtoday.viewmodel.MapsViewModel


private const val NUM_PAGES = 2

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private val mMapsViewModel: MapsViewModel by viewModels()
    private lateinit var regions: ArrayList<Region>
    var country = "Canada"
    var province = "Ontario"

    private lateinit var mPagerAdapter: FragmentStateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val covidIcon: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.ic_covid, null)
        val vaccineIcon: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.ic_vaccine, null)

        mPagerAdapter = MainPagerAdapter(this)
        getRegions()

        mBinding.mainPager.adapter = mPagerAdapter
        mBinding.locationChange.setOnClickListener {
            val list = ArrayList<String>()
            list.add("China")
            list.add("Canada")
            popup("Country", list)
        }

        TabLayoutMediator(mBinding.mainPagerTabs, mBinding.mainPager) { tab, position ->
            when(position) {
                0 -> {
                    tab.icon = covidIcon
                    tab.text = "Covid Info"
                }
                else -> {
                    tab.icon = vaccineIcon
                    tab.text = "Vaccine Info"
                }
            }
        }.attach()
        mBinding.mainPager.isUserInputEnabled = false
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        getLocationPermission()
        getPhoneCallPermission()
    }

    private fun hideSystemBars() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private inner class MainPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> CovidFragment()
                else -> VaccineFragment()
            }
        }
    }

    fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED) {
            mMapsViewModel.isLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun getPhoneCallPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE
            )
            == PackageManager.PERMISSION_GRANTED) {
            // do nothing
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    // Show a popup options list for location change
    private fun popup(word: String, list: ArrayList<String>) {
        val builderSingle = AlertDialog.Builder(this)
        var title = "Select a "
        title += word
        builderSingle.setTitle(title)

        builderSingle.setItems(
            list.toTypedArray()
        ) { _: DialogInterface?, which: Int ->
            var areaChanged = false
            if (word == "Country") {
                country = list[which]
                popup("Province", getProvinces(country))
            } else if (word == "Province") {
                if (province != list[which] || mBinding.locationCountryText.text != country) areaChanged = true
                province = list[which]
                mBinding.locationCountryText.text = country
                mBinding.locationProvinceText.text = province
            }

            // refresh fragments based on new area
            if (areaChanged) {
                (supportFragmentManager.findFragmentByTag("f0") as CovidFragment).fillDataSource()
            }
        }

        val dialog = builderSingle.create()
        dialog.show()

    }

    // Read region list from json file
    private fun getRegions() {
        val jsonFileString = getJsonDataFromAsset(applicationContext, "Regions.json")
        val gson = Gson()
        val listRegionType = object : TypeToken<List<Region>>() {}.type
        regions = gson.fromJson(jsonFileString, listRegionType)
    }

    // Return a list of provinces for a country
    private fun getProvinces(country: String): ArrayList<String> {
        val list = ArrayList<String>()
        list.add("All")
        if (country == "China") {
            regions.forEach {
                if (it.country == "CN")
                    list.add(it.name)
            }
        } else if (country == "Canada") {
            regions.forEach {
                if (it.country == "CA")
                    list.add(it.name)
            }
        }

        return list
    }
}
