package com.pucetec.mrcanchas.services

import com.pucetec.mrcanchas.models.*
import retrofit2.http.*

interface ApiService {

    @GET("courts/api/courts")
    suspend fun getCourts(): List<Court>

    @GET("courts/api/courts/{id}")
    suspend fun getCourt(
        @Path("id") id: Long
    ): Court

    @GET("courts/api/time-slots/court/{courtId}")
    suspend fun getTimeSlotsByCourt(
        @Path("courtId") courtId: Long
    ): List<TimeSlot>

    @POST("courts/api/reservations")
    suspend fun createReservation(
        @Body request: ReservationRequest
    ): Reservation

    @GET("courts/api/reservations/me")
    suspend fun getMyReservations(): List<Reservation>

    @GET("courts/api/reservations/{id}")
    suspend fun getReservation(
        @Path("id") id: Long
    ): Reservation

    @DELETE("courts/api/reservations/{id}")
    suspend fun cancelReservation(
        @Path("id") id: Long
    )

    @GET("courts/api/match-results/reservation/{reservationId}")
    suspend fun getMatchResult(
        @Path("reservationId") reservationId: Long
    ): MatchResult

    @POST("courts/api/match-results/reservation/{reservationId}")
    suspend fun createMatchResult(
        @Path("reservationId") reservationId: Long,
        @Body request: MatchResultRequest
    ): MatchResult

    // User profile endpoints (under /users context path in reverse proxy)
    @POST("users/api/users/me")
    suspend fun createMyProfile(
        @Body request: UserProfileRequest
    ): UserProfileResponse

    @GET("users/api/users/me")
    suspend fun getMyProfile(): UserProfileResponse

    @PUT("users/api/users/me")
    suspend fun updateMyProfile(
        @Body request: UserProfileRequest
    ): UserProfileResponse
}
