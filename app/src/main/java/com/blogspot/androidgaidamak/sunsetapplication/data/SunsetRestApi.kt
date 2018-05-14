package com.blogspot.androidgaidamak.sunsetapplication.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SunsetRestApi {
    @GET("json")
    fun getSunsetSunrise(@Query("lat") lat: Double, @Query("lng") lng: Double): Call<SunriseSunsetResponse>
}