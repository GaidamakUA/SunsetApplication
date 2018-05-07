package com.blogspot.androidgaidamak.sunsetapplication

import java.util.*

data class SunriseSunsetResponse(val results: Result, val status: String) {
}

data class Result(
        val sunrise: Date,
        val sunset: Date,
        val solar_noon: Date,
        val day_length: String,
        val civil_twilight_begin: Date,
        val civil_twilight_end: Date,
        val nautical_twilight_begin: Date,
        val nautical_twilight_end: Date,
        val astronomical_twilight_begin: Date,
        val astronomical_twilight_end: Date)