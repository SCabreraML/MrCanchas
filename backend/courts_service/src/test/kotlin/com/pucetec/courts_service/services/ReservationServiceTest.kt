package com.pucetec.courts_service.services

import com.pucetec.courts_service.dto.request.ReservationRequest
import com.pucetec.courts_service.dto.response.ReservationResponse
import com.pucetec.courts_service.entities.Court
import com.pucetec.courts_service.entities.Reservation
import com.pucetec.courts_service.entities.TimeSlot
import com.pucetec.courts_service.exceptions.InvalidReservationDurationException
import com.pucetec.courts_service.exceptions.ReservationInThePastException
import com.pucetec.courts_service.exceptions.ReservationNotFoundException
import com.pucetec.courts_service.exceptions.ReservationOutsideAllowedHoursException
import com.pucetec.courts_service.exceptions.TimeSlotAlreadyReservedException
import com.pucetec.courts_service.exceptions.TimeSlotNotFoundException
import com.pucetec.courts_service.exceptions.UnauthorizedReservationException
import com.pucetec.courts_service.mappers.ReservationMapper
import com.pucetec.courts_service.repositories.ReservationRepository
import com.pucetec.courts_service.repositories.TimeSlotRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ReservationServiceTest {

    @Mock
    private lateinit var reservationRepository: ReservationRepository

    @Mock
    private lateinit var timeSlotRepository: TimeSlotRepository

    @Mock
    private lateinit var reservationMapper: ReservationMapper

    @InjectMocks
    private lateinit var reservationService: ReservationService

    private val currentUser = "user-sub-123"

    private fun <T> anyNonNull(): T {
        Mockito.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

    @BeforeEach
    fun setUpSecurityContext() {
        val auth = UsernamePasswordAuthenticationToken(currentUser, null, emptyList())
        SecurityContextHolder.getContext().authentication = auth
    }

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    private fun sampleCourt() =
        Court(id = 1L, name = "Court A", sport = "Tennis", location = "Zone 1", available = true)

    private fun sampleTimeSlot(
        id: Long = 10L,
        date: LocalDate = LocalDate.of(2026, 12, 10),
        startTime: LocalTime = LocalTime.of(9, 0),
        endTime: LocalTime = LocalTime.of(10, 0),
        status: TimeSlot.Status = TimeSlot.Status.AVAILABLE
    ) = TimeSlot(
        id = id,
        court = sampleCourt(),
        date = date,
        startTime = startTime,
        endTime = endTime,
        status = status
    )

    private fun sampleReservation(
        id: Long = 100L,
        owner: String = currentUser,
        status: Reservation.Status = Reservation.Status.CONFIRMED,
        timeSlot: TimeSlot = sampleTimeSlot()
    ) = Reservation(
        id = id,
        timeSlot = timeSlot,
        ownerUser = owner,
        startDateTime = LocalDateTime.of(timeSlot.date, timeSlot.startTime),
        endDateTime = LocalDateTime.of(timeSlot.date, timeSlot.endTime),
        status = status
    )

    private fun sampleResponse(id: Long = 100L) =
        ReservationResponse(
            id = id,
            timeSlotId = 10L,
            ownerUser = currentUser,
            status = "CONFIRMED",
            createdAt = LocalDateTime.of(2026, 12, 10, 9, 0)
        )

    @Test
    fun `create makes a reservation when the time slot is free and valid`() {
        val request = ReservationRequest(timeSlotId = 10L)
        val timeSlot = sampleTimeSlot()
        val saved = sampleReservation(timeSlot = timeSlot)

        `when`(timeSlotRepository.findById(10L)).thenReturn(Optional.of(timeSlot))
        `when`(reservationRepository.existsByTimeSlotIdAndStatus(10L, Reservation.Status.CONFIRMED))
            .thenReturn(false)
        `when`(reservationRepository.save(anyNonNull())).thenReturn(saved)
        `when`(reservationMapper.toResponse(saved)).thenReturn(sampleResponse())

        val result = reservationService.create(request)

        assertEquals(100L, result.id)
        assertEquals(TimeSlot.Status.RESERVED, timeSlot.status)
        verify(timeSlotRepository).save(timeSlot)
    }

    @Test
    fun `create throws TimeSlotNotFoundException when the slot does not exist`() {
        val request = ReservationRequest(timeSlotId = 99L)
        `when`(timeSlotRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<TimeSlotNotFoundException> {
            reservationService.create(request)
        }
    }

    @Test
    fun `create throws ReservationInThePastException when start time is in the past`() {
        val request = ReservationRequest(timeSlotId = 10L)
        val pastSlot = sampleTimeSlot(date = LocalDate.of(2020, 1, 1))

        `when`(timeSlotRepository.findById(10L)).thenReturn(Optional.of(pastSlot))

        assertThrows<ReservationInThePastException> {
            reservationService.create(request)
        }
    }

    @Test
    fun `create throws ReservationOutsideAllowedHoursException when hours are outside allowed limit`() {
        val request = ReservationRequest(timeSlotId = 10L)
        // Inicio a las 06:00 (permitido solo a partir de las 07:00)
        val earlySlot = sampleTimeSlot(startTime = LocalTime.of(6, 0), endTime = LocalTime.of(8, 0))

        `when`(timeSlotRepository.findById(10L)).thenReturn(Optional.of(earlySlot))

        assertThrows<ReservationOutsideAllowedHoursException> {
            reservationService.create(request)
        }
    }

    @Test
    fun `create throws TimeSlotAlreadyReservedException when the slot is taken`() {
        val request = ReservationRequest(timeSlotId = 10L)
        val timeSlot = sampleTimeSlot()

        `when`(timeSlotRepository.findById(10L)).thenReturn(Optional.of(timeSlot))
        `when`(reservationRepository.existsByTimeSlotIdAndStatus(10L, Reservation.Status.CONFIRMED))
            .thenReturn(true)

        assertThrows<TimeSlotAlreadyReservedException> {
            reservationService.create(request)
        }
    }

    @Test
    fun `findMyReservations returns the current user reservations`() {
        val reservation = sampleReservation()
        `when`(reservationRepository.findByOwnerUser(currentUser)).thenReturn(listOf(reservation))
        `when`(reservationMapper.toResponse(reservation)).thenReturn(sampleResponse())

        val result = reservationService.findMyReservations()

        assertEquals(1, result.size)
        assertEquals(currentUser, result[0].ownerUser)
    }

    @Test
    fun `findById returns the reservation when it exists`() {
        val reservation = sampleReservation()
        `when`(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation))
        `when`(reservationMapper.toResponse(reservation)).thenReturn(sampleResponse())

        val result = reservationService.findById(100L)

        assertEquals(100L, result.id)
    }

    @Test
    fun `findById throws ReservationNotFoundException when it does not exist`() {
        `when`(reservationRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ReservationNotFoundException> {
            reservationService.findById(99L)
        }
    }

    @Test
    fun `cancel sets the reservation to cancelled when the owner requests it`() {
        val reservation = sampleReservation(owner = currentUser)
        `when`(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation))

        reservationService.cancel(100L)

        assertEquals(Reservation.Status.CANCELLED, reservation.status)
        assertEquals(TimeSlot.Status.AVAILABLE, reservation.timeSlot.status)
        verify(reservationRepository).save(reservation)
        verify(timeSlotRepository).save(reservation.timeSlot)
    }

    @Test
    fun `cancel throws ReservationNotFoundException when it does not exist`() {
        `when`(reservationRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ReservationNotFoundException> {
            reservationService.cancel(99L)
        }
    }

    @Test
    fun `cancel throws UnauthorizedReservationException when another user requests it`() {
        val reservation = sampleReservation(owner = "another-user")
        `when`(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation))

        assertThrows<UnauthorizedReservationException> {
            reservationService.cancel(100L)
        }
        verify(reservationRepository, never()).save(reservation)
    }
}