package com.zenithsky.app.data.remote.dto

data class AirportDto(
    val airportId: Int,
    val iata: String,
    val icao: String,
    val name: String
)


data class AirportRequest(
    val iata: String,
    val icao: String,
    val name: String
)