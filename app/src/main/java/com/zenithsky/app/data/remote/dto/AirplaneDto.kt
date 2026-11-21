package com.zenithsky.app.data.remote.dto

data class AirplaneDto(
    val airplaneId: Int,
    val capacity: Int,
    val airplaneType: AirplaneTypeDto,
    val airline: AirlineDto
)

data class AirplaneRequest(
    val capacity: Int,
    val typeId: Int,
    val airlineId: Int
)