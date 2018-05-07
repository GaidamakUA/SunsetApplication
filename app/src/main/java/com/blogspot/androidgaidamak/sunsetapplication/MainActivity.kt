package com.blogspot.androidgaidamak.sunsetapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.util.Log
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.DateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1
    private val PLACE_PICKER_REQUEST = 1
    private lateinit var DISPLAY_TIME_FORMAT: DateFormat

    private var builder = PlacePicker.IntentBuilder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var myLocation: Location? = null
    private var activeLocation: Location? = null

    private val api: SunsetRestApi

    init {
        val client = OkHttpClient().newBuilder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
                })
                .build()
        val moshi = Moshi.Builder().add(Date::class.javaObjectType, DateJsonAdapter()).build()
        api = Retrofit.Builder()
                .baseUrl("https://api.sunrise-sunset.org")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(client)
                .build()
                .create(SunsetRestApi::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pickLocationButton.setOnClickListener {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        }
        myLocationButton.setOnClickListener {
            if (myLocation == null) {
                Toast.makeText(this, "Location isn't available", Toast.LENGTH_LONG).show()
            } else {
                title = "My location"
                activeLocation = myLocation
                refreshSunriseSunset()
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initMyLocation()

        DISPLAY_TIME_FORMAT = android.text.format.DateFormat.getTimeFormat(getApplicationContext())
    }

    private fun initMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        myLocation = location
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
            title = toastMsg

            activeLocation = Location(LocationManager.GPS_PROVIDER);
            activeLocation?.latitude = place.latLng.latitude
            activeLocation?.longitude = place.latLng.longitude
            refreshSunriseSunset()
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

    private fun refreshSunriseSunset() {
        if (activeLocation == null) return
        api.getSunsetSunrise(activeLocation!!.latitude, activeLocation!!.longitude)
                .enqueue(object : Callback<SunriseSunsetResponse> {
                    override fun onFailure(call: Call<SunriseSunsetResponse>?, t: Throwable?) {
                        Log.d(TAG, "onResponse: call:$call, t:$t")
                    }

                    override fun onResponse(call: Call<SunriseSunsetResponse>?, response: Response<SunriseSunsetResponse>?) {
                        val results = response?.body()?.results
                        sunriseTextView.text = DISPLAY_TIME_FORMAT.format(results?.sunrise)
                        sunsetTextView.text = DISPLAY_TIME_FORMAT.format(results?.sunset)
                    }
                })
    }
}
