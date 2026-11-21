package com.zenithsky.app.data.remote.dto


import com.google.gson.annotations.SerializedName


data class AirlineDto(
    val airlineId: Int,
    val iata: String,
    val airlineName: String,
    val baseAirport: AirportDto?
)


data class AirlineRequest(
    val iata: String,
    @SerializedName("airlinename") val airlineName: String,
    val baseAirport: Int
)