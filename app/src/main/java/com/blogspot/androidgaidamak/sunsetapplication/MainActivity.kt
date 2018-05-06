package com.blogspot.androidgaidamak.sunsetapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.support.annotation.NonNull




class MainActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1
    private val PLACE_PICKER_REQUEST = 1
    private var builder = PlacePicker.IntentBuilder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pickLocationButton.setOnClickListener {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initMyLocation()
    }


    private fun initMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        val toastMsg = String.format("Location: %s", location.toString())
                        Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show()
                        // Got last known location. In some rare situations this can be null.
                    }
        } else {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_COARSE_LOCATION, true);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val place = PlacePicker.getPlace(this, data)
            val toastMsg = String.format("Place: %s", place.name)
            Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Enable the my location layer if the permission has been granted.
                initMyLocation()
            }
        }
    }
}
