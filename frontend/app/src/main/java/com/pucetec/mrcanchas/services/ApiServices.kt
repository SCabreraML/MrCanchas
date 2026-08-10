interface ApiService {

    @GET("api/courts")
    suspend fun getCourts(): List<Court>

    @GET("api/courts/{id}")
    suspend fun getCourt(
        @Path("id") id: Long
    ): Court

    @GET("api/time-slots/court/{courtId}")
    suspend fun getTimeSlotsByCourt(
        @Path("courtId") courtId: Long
    ): List<TimeSlot>

    @POST("api/reservations")
    suspend fun createReservation(
        @Body request: ReservationRequest
    ): Reservation

    @GET("api/reservations/me")
    suspend fun getMyReservations(): List<Reservation>

    @GET("api/reservations/{id}")
    suspend fun getReservation(
        @Path("id") id: Long
    ): Reservation

    @DELETE("api/reservations/{id}")
    suspend fun cancelReservation(
        @Path("id") id: Long
    )

    @GET("api/match-results/reservation/{reservationId}")
    suspend fun getMatchResult(
        @Path("reservationId") reservationId: Long
    ): MatchResult
}