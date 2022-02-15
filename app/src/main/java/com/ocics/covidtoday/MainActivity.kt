package com.ocics.covidtoday

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.ocics.covidtoday.databinding.ActivityMainBinding
import com.ocics.covidtoday.fragment.CovidFragment
import com.ocics.covidtoday.fragment.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.ocics.covidtoday.fragment.VaccineFragment
import com.ocics.covidtoday.viewmodel.MapsViewModel

private const val NUM_PAGES = 2

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private val mMapsViewModel: MapsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val covidIcon: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.ic_covid, null)
        val vaccineIcon: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.ic_vaccine, null)

        val pagerAdapter = MainPagerAdapter(this)
        mBinding.mainPager.adapter = pagerAdapter
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
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        getLocationPermission()
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

}