package com.ocics.covidtoday.adapter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.maps.model.PlaceDetails
import com.ocics.covidtoday.R
import com.ocics.covidtoday.fragment.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION


class PlacesRecyclerAdapter(): RecyclerView.Adapter<PlacesRecyclerAdapter.PlaceViewHolder>() {
    var mPlacesList: ArrayList<PlaceDetails>? = ArrayList()
    private var mClickListener: ClickListener? = null

    interface ClickListener {
        fun onClickCallback(index: Int)
    }

    fun setClickListener(clickListener: ClickListener) {
        mClickListener = clickListener
    }

    inner class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val placeName: TextView
        val placeAddr: TextView
        val placePhone: TextView
        val callButton: ImageButton

        init {
            placeName = view.findViewById(R.id.place_item_name)
            placeAddr = view.findViewById(R.id.place_item_address)
            placePhone = view.findViewById(R.id.place_item_phone)
            callButton = view.findViewById(R.id.place_item_call_button)
            callButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + placePhone.text))
                if (ContextCompat.checkSelfPermission(view.context,
                        Manifest.permission.CALL_PHONE
                    )
                    == PackageManager.PERMISSION_GRANTED) {
                    view.context.startActivity(intent)
                } else {
                    Toast.makeText(view.context, "No Phone Call Permission! Please relaunch.", Toast.LENGTH_LONG)
                }
            }

            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            mClickListener?.onClickCallback(adapterPosition)
        }
    }

    override fun onBindViewHolder(viewHolder: PlaceViewHolder, i: Int) {
        viewHolder.placeName.text = mPlacesList!![i].name
        viewHolder.placeAddr.text = mPlacesList!![i].formattedAddress.split(",")[0]
        viewHolder.placePhone.text = mPlacesList!![i].internationalPhoneNumber
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.place_item, parent, false)
        return PlaceViewHolder(view)
    }

    override fun getItemCount() = mPlacesList!!.size


}