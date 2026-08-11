package com.pucetec.courts_service.services

import com.pucetec.courts_service.dto.request.MatchResultRequest
import com.pucetec.courts_service.dto.request.TeamScoreRequest
import com.pucetec.courts_service.dto.response.MatchResultResponse
import com.pucetec.courts_service.entities.Court
import com.pucetec.courts_service.entities.MatchResult
import com.pucetec.courts_service.entities.Reservation
import com.pucetec.courts_service.entities.TimeSlot
import com.pucetec.courts_service.exceptions.InvalidTeamsException
import com.pucetec.courts_service.exceptions.InvalidWinnerException
import com.pucetec.courts_service.exceptions.MatchResultAlreadyExistsException
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
import org.mockito.Mockito.verify
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
            date = LocalDate.of(2026, 12, 10),
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
            status = TimeSlot.Status.AVAILABLE
        )

    private fun sampleReservation() =
        Reservation(
            id = 100L,
            timeSlot = sampleTimeSlot(),
            ownerUser = "user1",
            startDateTime = LocalDateTime.of(2026, 12, 10, 9, 0),
            endDateTime = LocalDateTime.of(2026, 12, 10, 10, 0),
            status = Reservation.Status.CONFIRMED
        )

    private fun sampleMatchResult() =
        MatchResult(
            id = 50L,
            reservation = sampleReservation(),
            teamA = "Team Red",
            scoreA = 3,
            teamB = "Team Blue",
            scoreB = 1,
            winner = "Team Red",
            status = MatchResult.MatchStatus.FINISHED,
            playedAt = LocalDateTime.of(2026, 12, 10, 10, 0)
        )

    private fun sampleRequest(winner: String? = "Team Red") =
        MatchResultRequest(
            teams = listOf(
                TeamScoreRequest(name = "Team Red", score = 3),
                TeamScoreRequest(name = "Team Blue", score = 1)
            ),
            winner = winner,
            status = MatchResult.MatchStatus.FINISHED
        )

    private fun sampleResponse() =
        MatchResultResponse(
            id = 50L,
            reservationId = 100L,
            status = MatchResult.MatchStatus.FINISHED.name,
            teamA = "Team Red",
            teamB = "Team Blue",
            scoreA = 3,
            scoreB = 1,
            winner = "Team Red",
            playedAt = LocalDateTime.of(2026, 12, 10, 10, 0)
        )

    @Test
    fun `findByReservation returns match result when exists`() {
        val matchResult = sampleMatchResult()

        `when`(matchResultRepository.findByReservationId(100L)).thenReturn(matchResult)
        `when`(matchResultMapper.toResponse(matchResult)).thenReturn(sampleResponse())

        val result = matchResultService.findByReservation(100L)

        assertEquals(50L, result.id)
    }

    @Test
    fun `findByReservation throws MatchResultNotFoundException when not exists`() {
        `when`(matchResultRepository.findByReservationId(100L)).thenReturn(null)

        assertThrows<MatchResultNotFoundException> {
            matchResultService.findByReservation(100L)
        }
    }

    @Test
    fun `create saves and returns match result when valid`() {
        val reservation = sampleReservation()
        val request = sampleRequest()
        val matchResult = sampleMatchResult()

        `when`(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation))
        `when`(matchResultRepository.findByReservationId(100L)).thenReturn(null)
        `when`(matchResultRepository.save(anyNonNull())).thenReturn(matchResult)
        `when`(matchResultMapper.toResponse(matchResult)).thenReturn(sampleResponse())

        val result = matchResultService.create(100L, request)

        assertEquals(50L, result.id)
        verify(matchResultRepository).save(anyNonNull())
    }

    @Test
    fun `create throws ReservationNotFoundException when reservation missing`() {
        val request = sampleRequest()
        `when`(reservationRepository.findById(100L)).thenReturn(Optional.empty())

        assertThrows<ReservationNotFoundException> {
            matchResultService.create(100L, request)
        }
    }

    @Test
    fun `create throws MatchResultAlreadyExistsException when result already registered`() {
        val reservation = sampleReservation()
        val request = sampleRequest()

        `when`(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation))
        `when`(matchResultRepository.findByReservationId(100L)).thenReturn(sampleMatchResult())

        assertThrows<MatchResultAlreadyExistsException> {
            matchResultService.create(100L, request)
        }
    }

    @Test
    fun `create throws InvalidTeamsException when team names are identical`() {
        val reservation = sampleReservation()
        val request = MatchResultRequest(
            teams = listOf(
                TeamScoreRequest(name = "Team Red", score = 3),
                TeamScoreRequest(name = "TEAM RED", score = 1)
            ),
            status = MatchResult.MatchStatus.FINISHED
        )

        `when`(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation))
        `when`(matchResultRepository.findByReservationId(100L)).thenReturn(null)

        assertThrows<InvalidTeamsException> {
            matchResultService.create(100L, request)
        }
    }

    @Test
    fun `create throws InvalidWinnerException when winner is not in team list`() {
        val reservation = sampleReservation()
        val request = sampleRequest(winner = "Unknown Team")

        `when`(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation))
        `when`(matchResultRepository.findByReservationId(100L)).thenReturn(null)

        assertThrows<InvalidWinnerException> {
            matchResultService.create(100L, request)
        }
    }
}