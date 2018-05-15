package com.blogspot.androidgaidamak.sunsetapplication

import android.Manifest
import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.blogspot.androidgaidamak.sunsetapplication.data.SunriseSunsetResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat

// I was thinking about adding MVVM architecture.
// But decided that it's too small for that yet.
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private val ACTIVE_LOCATION_KEY = "active_location"
    private val FIRST_REQUEST_KEY = "first_request"
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1
    private val PLACE_PICKER_REQUEST = 1
    private lateinit var DISPLAY_TIME_FORMAT: DateFormat
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var colorAnimator: ValueAnimator

    private var myLocation: Location? = null
    private var activeLocation: Location? = null
    private var firstRequest = true
    private var call: Call<SunriseSunsetResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initContextDependedConstants()

        initUiListeners()
        restoreState(savedInstanceState)
        initMyLocation()

        refreshSunriseSunset()
    }

    private fun initContextDependedConstants() {
        if (!::DISPLAY_TIME_FORMAT.isInitialized) {
            DISPLAY_TIME_FORMAT = android.text.format.DateFormat.getTimeFormat(getApplicationContext())
        }
        if (!::colorAnimator.isInitialized) {
            colorAnimator = AnimatorInflater.loadAnimator(this, R.animator.color_animator) as ValueAnimator
            colorAnimator.addUpdateListener {
                ImageViewCompat.setImageTintList(sunriseIcon, ColorStateList.valueOf(colorAnimator.animatedValue as Int));
                ImageViewCompat.setImageTintList(sunsetIcon, ColorStateList.valueOf(colorAnimator.animatedValue as Int));
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun initUiListeners() {
        pickLocationButton.setOnClickListener {
            val intent = PlacePicker.IntentBuilder().build(this)
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        }
        myLocationButton.setOnClickListener {
            if (myLocation == null) {
                Toast.makeText(this, R.string.location_isnt_available, Toast.LENGTH_LONG).show()
            } else {
                locationTitleText.text = getString(R.string.my_loaction_title)
                activeLocation = myLocation
                refreshSunriseSunset()
            }
        }
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            firstRequest = savedInstanceState.getBoolean(FIRST_REQUEST_KEY, true)
            activeLocation = savedInstanceState.getParcelable(ACTIVE_LOCATION_KEY)
        }
    }

    private fun initMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        myLocation = location
                    }
        } else if (firstRequest) {
            firstRequest = false;
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_COARSE_LOCATION, true);
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable(ACTIVE_LOCATION_KEY, activeLocation)
        outState?.putBoolean(FIRST_REQUEST_KEY, firstRequest)
    }

    override fun onStop() {
        super.onStop()
        call?.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val place = PlacePicker.getPlace(this, data)
            locationTitleText.text = getString(R.string.place_title_pattern, place.name)

            activeLocation = Location(LocationManager.GPS_PROVIDER);
            activeLocation?.latitude = place.latLng.latitude
            activeLocation?.longitude = place.latLng.longitude
            refreshSunriseSunset()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
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
        startAnimatingIcons()
        call?.cancel()
        call = ServiceLocator.sunsetApi.getSunsetSunrise(activeLocation!!.latitude, activeLocation!!.longitude)
        call?.enqueue(object : Callback<SunriseSunsetResponse> {
            override fun onFailure(call: Call<SunriseSunsetResponse>?, t: Throwable?) {
                animateIconsLoadingFailed()
                Toast.makeText(this@MainActivity, R.string.loading_failed, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<SunriseSunsetResponse>?, response: Response<SunriseSunsetResponse>?) {
                val results = response?.body()?.results
                sunriseTextView.text = DISPLAY_TIME_FORMAT.format(results?.sunrise)
                sunsetTextView.text = DISPLAY_TIME_FORMAT.format(results?.sunset)
                stopAnimatingIcons()
            }
        })
    }

    private fun startAnimatingIcons() {
        colorAnimator.repeatCount = -1
        colorAnimator.start()
    }

    private fun animateIconsLoadingFailed() {
        colorAnimator.repeatCount = 0
        colorAnimator.reverse()
    }

    private fun stopAnimatingIcons() {
        colorAnimator.repeatCount = 0
    }
}
