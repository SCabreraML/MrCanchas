package com.pucetec.courts_service.services

import com.pucetec.courts_service.dto.request.TimeSlotRequest
import com.pucetec.courts_service.dto.response.TimeSlotResponse
import com.pucetec.courts_service.entities.Court
import com.pucetec.courts_service.entities.TimeSlot
import com.pucetec.courts_service.exceptions.CourtNotFoundException
import com.pucetec.courts_service.exceptions.TimeSlotNotFoundException
import com.pucetec.courts_service.mappers.TimeSlotMapper
import com.pucetec.courts_service.repositories.CourtRepository
import com.pucetec.courts_service.repositories.TimeSlotRepository
import org.junit.jupiter.api.Assertions.assertEquals
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
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class TimeSlotServiceTest {

    @Mock
    private lateinit var timeSlotRepository: TimeSlotRepository

    @Mock
    private lateinit var courtRepository: CourtRepository

    @Mock
    private lateinit var timeSlotMapper: TimeSlotMapper

    @InjectMocks
    private lateinit var timeSlotService: TimeSlotService

    private fun <T> anyNonNull(): T {
        Mockito.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

    private fun sampleCourt() =
        Court(id = 1L, name = "Court A", sport = "Tennis", location = "Zone 1", available = true)

    private fun sampleTimeSlot(id: Long = 10L) =
        TimeSlot(
            id = id,
            court = sampleCourt(),
            date = LocalDate.of(2026, 8, 10),
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
            status = TimeSlot.Status.AVAILABLE
        )

    private fun sampleRequest() =
        TimeSlotRequest(
            courtId = 1L,
            date = LocalDate.of(2026, 8, 10),
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0)
        )

    private fun sampleResponse(id: Long = 10L) =
        TimeSlotResponse(
            id = id,
            courtId = 1L,
            date = LocalDate.of(2026, 8, 10),
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
            status = "AVAILABLE"
        )

    @Test
    fun `findAll returns mapped time slots`() {
        val slot = sampleTimeSlot()
        `when`(timeSlotRepository.findAll()).thenReturn(listOf(slot))
        `when`(timeSlotMapper.toResponse(slot)).thenReturn(sampleResponse())

        val result = timeSlotService.findAll()

        assertEquals(1, result.size)
    }

    @Test
    fun `findByCourt returns the slots when the court exists`() {
        val slot = sampleTimeSlot()
        `when`(courtRepository.existsById(1L)).thenReturn(true)
        `when`(timeSlotRepository.findByCourtId(1L)).thenReturn(listOf(slot))
        `when`(timeSlotMapper.toResponse(slot)).thenReturn(sampleResponse())

        val result = timeSlotService.findByCourt(1L)

        assertEquals(1, result.size)
    }

    @Test
    fun `findByCourt throws CourtNotFoundException when the court does not exist`() {
        `when`(courtRepository.existsById(99L)).thenReturn(false)

        assertThrows<CourtNotFoundException> {
            timeSlotService.findByCourt(99L)
        }
    }

    @Test
    fun `findById returns the slot when it exists`() {
        val slot = sampleTimeSlot()
        `when`(timeSlotRepository.findById(10L)).thenReturn(Optional.of(slot))
        `when`(timeSlotMapper.toResponse(slot)).thenReturn(sampleResponse())

        val result = timeSlotService.findById(10L)

        assertEquals(10L, result.id)
    }

    @Test
    fun `findById throws TimeSlotNotFoundException when it does not exist`() {
        `when`(timeSlotRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<TimeSlotNotFoundException> {
            timeSlotService.findById(99L)
        }
    }

    @Test
    fun `create saves and returns the slot when the court exists and times are valid`() {
        val court = sampleCourt()
        val slot = sampleTimeSlot()
        val request = sampleRequest()

        `when`(courtRepository.findById(1L)).thenReturn(Optional.of(court))
        `when`(timeSlotMapper.toEntity(request, court)).thenReturn(slot)
        `when`(timeSlotRepository.save(slot)).thenReturn(slot)
        `when`(timeSlotMapper.toResponse(slot)).thenReturn(sampleResponse())

        val result = timeSlotService.create(request)

        assertEquals(10L, result.id)
    }

    @Test
    fun `create throws CourtNotFoundException when the court does not exist`() {
        val request = sampleRequest()
        `when`(courtRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<CourtNotFoundException> {
            timeSlotService.create(request)
        }
    }

    @Test
    fun `create throws when start time is not before end time`() {
        val court = sampleCourt()
        val request = TimeSlotRequest(
            courtId = 1L,
            date = LocalDate.of(2026, 8, 10),
            startTime = LocalTime.of(11, 0),
            endTime = LocalTime.of(10, 0)
        )
        `when`(courtRepository.findById(1L)).thenReturn(Optional.of(court))

        assertThrows<IllegalArgumentException> {
            timeSlotService.create(request)
        }
    }

    @Test
    fun `update modifies and returns the slot when it exists and times are valid`() {
        val slot = sampleTimeSlot()
        val court = sampleCourt()
        val request = sampleRequest()

        `when`(timeSlotRepository.findById(10L)).thenReturn(Optional.of(slot))
        `when`(courtRepository.findById(1L)).thenReturn(Optional.of(court))
        `when`(timeSlotRepository.save(slot)).thenReturn(slot)
        `when`(timeSlotMapper.toResponse(slot)).thenReturn(sampleResponse())

        val result = timeSlotService.update(10L, request)

        assertEquals(10L, result.id)
    }

    @Test
    fun `update throws TimeSlotNotFoundException when the slot does not exist`() {
        val request = sampleRequest()
        `when`(timeSlotRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<TimeSlotNotFoundException> {
            timeSlotService.update(99L, request)
        }
    }

    @Test
    fun `update throws CourtNotFoundException when the court does not exist`() {
        val slot = sampleTimeSlot()
        val request = sampleRequest()
        `when`(timeSlotRepository.findById(10L)).thenReturn(Optional.of(slot))
        `when`(courtRepository.findById(1L)).thenReturn(Optional.empty())

        assertThrows<CourtNotFoundException> {
            timeSlotService.update(10L, request)
        }
    }

    @Test
    fun `update throws when start time is not before end time`() {
        val slot = sampleTimeSlot()
        val court = sampleCourt()
        val request = TimeSlotRequest(
            courtId = 1L,
            date = LocalDate.of(2026, 8, 10),
            startTime = LocalTime.of(11, 0),
            endTime = LocalTime.of(10, 0)
        )
        `when`(timeSlotRepository.findById(10L)).thenReturn(Optional.of(slot))
        `when`(courtRepository.findById(1L)).thenReturn(Optional.of(court))

        assertThrows<IllegalArgumentException> {
            timeSlotService.update(10L, request)
        }
    }

    @Test
    fun `delete removes the slot when it exists`() {
        `when`(timeSlotRepository.existsById(10L)).thenReturn(true)

        timeSlotService.delete(10L)

        verify(timeSlotRepository).deleteById(10L)
    }

    @Test
    fun `delete throws TimeSlotNotFoundException when it does not exist`() {
        `when`(timeSlotRepository.existsById(99L)).thenReturn(false)

        assertThrows<TimeSlotNotFoundException> {
            timeSlotService.delete(99L)
        }
        verify(timeSlotRepository, never()).deleteById(99L)
    }
}