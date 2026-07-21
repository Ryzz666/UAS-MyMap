package com.naufal.mymap.data.api

import com.naufal.mymap.data.model.DirectionsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApiService {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String? = null,
        @Query("key") apiKey: String
    ): DirectionsResponse
}
