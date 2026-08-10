package com.pucetec.courts_service.services

import com.pucetec.courts_service.dto.request.MatchResultRequest
import com.pucetec.courts_service.dto.response.MatchResultResponse
import com.pucetec.courts_service.entities.Court
import com.pucetec.courts_service.entities.MatchResult
import com.pucetec.courts_service.entities.Reservation
import com.pucetec.courts_service.entities.TimeSlot
import com.pucetec.courts_service.exceptions.MatchResultNotFoundException
import com.pucetec.courts_service.exceptions.ReservationNotFoundException
import com.pucetec.courts_service.mappers.MatchResultMapper
import com.pucetec.courts_service.repositories.MatchResultRepository
import com.pucetec.courts_service.repositories.ReservationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MatchResultServiceTest {

    @Mock
    private lateinit var matchResultRepository: MatchResultRepository

    @Mock
    private lateinit var reservationRepository: ReservationRepository

    @Mock
    private lateinit var matchResultMapper: MatchResultMapper

    @InjectMocks
    private lateinit var matchResultService: MatchResultService

    private fun <T> anyNonNull(): T {
        Mockito.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

    private fun sampleCourt() =
        Court(id = 1L, name = "Court A", sport = "Tennis", location = "Zone 1", available = true)

    private fun sampleTimeSlot() =
        TimeSlot(
            id = 10L,
            court = sampleCourt(),
            date = LocalDate.of(2026, 8, 10),
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
            status = TimeSlot.Status.RESERVED
        )

    private fun sampleReservation(id: Long = 100L) =
        Reservation(
            id = id,
            timeSlot = sampleTimeSlot(),
            ownerUser = "user-sub-123",
            status = Reservation.Status.CONFIRMED,
            createdAt = LocalDateTime.of(2026, 8, 5, 12, 0)
        )

    private fun sampleRequest() =
        MatchResultRequest(
            teamA = "Team A",
            teamB = "Team B",
            scoreA = 3,
            scoreB = 1,
            winner = "Team A",
            playedAt = LocalDateTime.of(2026, 8, 10, 10, 0)
        )

    private fun sampleMatchResult(reservation: Reservation) =
        MatchResult(
            id = 500L,
            reservation = reservation,
            teamA = "Team A",
            teamB = "Team B",
            scoreA = 3,
            scoreB = 1,
            winner = "Team A",
            playedAt = LocalDateTime.of(2026, 8, 10, 10, 0)
        )

    private fun sampleResponse() =
        MatchResultResponse(
            id = 500L,
            reservationId = 100L,
            teamA = "Team A",
            teamB = "Team B",
            scoreA = 3,
            scoreB = 1,
            winner = "Team A",
            playedAt = LocalDateTime.of(2026, 8, 10, 10, 0)
        )

    @Test
    fun `create saves the result and completes the reservation`() {
        val reservation = sampleReservation()
        val request = sampleRequest()
        val entity = sampleMatchResult(reservation)

        `when`(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation))
        `when`(matchResultMapper.toEntity(request, reservation)).thenReturn(entity)
        `when`(matchResultRepository.save(entity)).thenReturn(entity)
        `when`(matchResultMapper.toResponse(entity)).thenReturn(sampleResponse())

        val result = matchResultService.create(100L, request)

        assertEquals(500L, result.id)
        assertEquals(Reservation.Status.COMPLETED, reservation.status)
    }

    @Test
    fun `create throws ReservationNotFoundException when the reservation does not exist`() {
        val request = sampleRequest()
        `when`(reservationRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ReservationNotFoundException> {
            matchResultService.create(99L, request)
        }
    }

    @Test
    fun `findByReservation returns the result when it exists`() {
        val reservation = sampleReservation()
        val entity = sampleMatchResult(reservation)
        `when`(matchResultRepository.findByReservationId(100L)).thenReturn(entity)
        `when`(matchResultMapper.toResponse(entity)).thenReturn(sampleResponse())

        val result = matchResultService.findByReservation(100L)

        assertEquals(500L, result.id)
    }

    @Test
    fun `findByReservation throws MatchResultNotFoundException when it does not exist`() {
        `when`(matchResultRepository.findByReservationId(99L)).thenReturn(null)

        assertThrows<MatchResultNotFoundException> {
            matchResultService.findByReservation(99L)
        }
    }
}