package com.pucetec.mrcanchas.services

import com.pucetec.mrcanchas.models.*
import retrofit2.http.*
import retrofit2.Response

interface ApiService {

    // ---------- CANCHAS ----------
    @GET("courts/api/courts")
    suspend fun getCourts(): List<Court>

    @GET("courts/api/courts/{id}")
    suspend fun getCourt(
        @Path("id") id: Long
    ): Court

    // ADMIN: crear cancha
    @POST("courts/api/courts")
    suspend fun createCourt(
        @Body request: CourtRequest
    ): Court

    // ADMIN: actualizar cancha
    @PUT("courts/api/courts/{id}")
    suspend fun updateCourt(
        @Path("id") id: Long,
        @Body request: CourtRequest
    ): Court

    // ADMIN: eliminar cancha
    @DELETE("courts/api/courts/{id}")
    suspend fun deleteCourt(
        @Path("id") id: Long
    ): Response<Unit>

    // ---------- HORARIOS ----------
    @GET("courts/api/time-slots/court/{courtId}")
    suspend fun getTimeSlotsByCourt(
        @Path("courtId") courtId: Long
    ): List<TimeSlot>

    // ADMIN: crear horario
    @POST("courts/api/time-slots")
    suspend fun createTimeSlot(
        @Body request: TimeSlotRequest
    ): TimeSlot

    // ADMIN: actualizar horario
    @PUT("courts/api/time-slots/{id}")
    suspend fun updateTimeSlot(
        @Path("id") id: Long,
        @Body request: TimeSlotRequest
    ): TimeSlot

    // ADMIN: eliminar horario
    @DELETE("courts/api/time-slots/{id}")
    suspend fun deleteTimeSlot(
        @Path("id") id: Long
    )

    // ---------- RESERVAS ----------
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

    // ---------- RESULTADOS ----------
    @GET("courts/api/match-results/reservation/{reservationId}")
    suspend fun getMatchResult(
        @Path("reservationId") reservationId: Long
    ): MatchResult

    @POST("courts/api/match-results/reservation/{reservationId}")
    suspend fun createMatchResult(
        @Path("reservationId") reservationId: Long,
        @Body request: MatchResultRequest
    ): MatchResult

    // ---------- PERFIL DE USUARIO ----------
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