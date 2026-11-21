package com.zenithsky.app.data.remote


import com.zenithsky.app.data.remote.dto.AirlineDto
import com.zenithsky.app.data.remote.dto.AirlineRequest
import com.zenithsky.app.data.remote.dto.AirplaneDto
import com.zenithsky.app.data.remote.dto.AirplaneRequest
import com.zenithsky.app.data.remote.dto.AirplaneTypeDto
import com.zenithsky.app.data.remote.dto.AirportDto
import com.zenithsky.app.data.remote.dto.AirportRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // aeropurtos
    @GET("api/airports")
    suspend fun getAirports(@Query("search") search: String? = null): Response<List<AirportDto>>

    @GET("api/airports/{id}")
    suspend fun getAirport(@Path("id") id: Int): Response<AirportDto>

    @POST("api/airports")
    suspend fun createAirport(@Body request: AirportRequest): Response<AirportDto>

    @PUT("api/airports/{id}")
    suspend fun updateAirport(@Path("id") id: Int, @Body request: AirportRequest): Response<AirportDto>

    @DELETE("api/airports/{id}")
    suspend fun deleteAirport(@Path("id") id: Int): Response<Unit>

    // aerolineas
    @GET("api/airlines")
    suspend fun getAirlines(): Response<List<AirlineDto>>

    @GET("api/airlines/{id}")
    suspend fun getAirline(@Path("id") id: Int): Response<AirlineDto>

    @POST("api/airlines")
    suspend fun createAirline(@Body request: AirlineRequest): Response<AirlineDto>

    @PUT("api/airlines/{id}")
    suspend fun updateAirline(@Path("id") id: Int, @Body request: AirlineRequest): Response<AirlineDto>

    @DELETE("api/airlines/{id}")
    suspend fun deleteAirline(@Path("id") id: Int): Response<Unit>

    // aviones
    @GET("api/airplanes")
    suspend fun getAirplanes(
        @Query("typeId") typeId: Int? = null,
        @Query("airlineId") airlineId: Int? = null
    ): Response<List<AirplaneDto>>

    @GET("api/airplanes/{id}")
    suspend fun getAirplane(@Path("id") id: Int): Response<AirplaneDto>

    @POST("api/airplanes")
    suspend fun createAirplane(@Body request: AirplaneRequest): Response<AirplaneDto>

    @PUT("api/airplanes/{id}")
    suspend fun updateAirplane(@Path("id") id: Int, @Body request: AirplaneRequest): Response<AirplaneDto>

    @DELETE("api/airplanes/{id}")
    suspend fun deleteAirplane(@Path("id") id: Int): Response<Unit>

    // tipos
    @GET("api/airplane-types")
    suspend fun getAirplaneTypes(): Response<List<AirplaneTypeDto>>
}