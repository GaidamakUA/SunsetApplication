package com.blogspot.androidgaidamak.sunsetapplication

import android.app.Application

class SunsetApplication : Application() {
    // For early initialization and making sure it won't be garbage collected
    private val serviceLocatorReference = ServiceLocator
}