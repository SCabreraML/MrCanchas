package com.pucetec.courts_service.services

import com.pucetec.courts_service.dto.request.CourtRequest
import com.pucetec.courts_service.dto.response.CourtResponse
import com.pucetec.courts_service.entities.Court
import com.pucetec.courts_service.exceptions.CourtNotFoundException
import com.pucetec.courts_service.exceptions.DuplicateCourtNameException
import com.pucetec.courts_service.mappers.CourtMapper
import com.pucetec.courts_service.repositories.CourtRepository
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
import java.util.Optional
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CourtServiceTest {
    // ...
}

@ExtendWith(MockitoExtension::class)
class CourtServiceTest {

    @Mock
    private lateinit var courtRepository: CourtRepository

    @Mock
    private lateinit var courtMapper: CourtMapper

    @InjectMocks
    private lateinit var courtService: CourtService

    private fun <T> anyNonNull(): T {
        Mockito.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

    private fun sampleCourt(id: Long = 1L, name: String = "Court A") =
        Court(id = id, name = name, sport = "Tennis", location = "Zone 1", available = true)

    private fun sampleRequest(name: String = "Court A") =
        CourtRequest(name = name, sport = "Tennis", location = "Zone 1", available = true)

    private fun sampleResponse(id: Long = 1L, name: String = "Court A") =
        CourtResponse(id = id, name = name, sport = "Tennis", location = "Zone 1", available = true)

    @Test
    fun `findAll returns mapped courts`() {
        val court = sampleCourt()
        `when`(courtRepository.findAll()).thenReturn(listOf(court))
        `when`(courtMapper.toResponse(court)).thenReturn(sampleResponse())

        val result = courtService.findAll()

        assertEquals(1, result.size)
        assertEquals("Court A", result[0].name)
    }

    @Test
    fun `findById returns court when exists`() {
        val court = sampleCourt()
        `when`(courtRepository.findById(1L)).thenReturn(Optional.of(court))
        `when`(courtMapper.toResponse(court)).thenReturn(sampleResponse())

        val result = courtService.findById(1L)

        assertEquals("Court A", result.name)
    }

    @Test
    fun `findById throws CourtNotFoundException when not exists`() {
        `when`(courtRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<CourtNotFoundException> {
            courtService.findById(99L)
        }
    }

    @Test
    fun `create saves court when name is unique`() {
        val request = sampleRequest()
        val court = sampleCourt()

        `when`(courtRepository.existsByName("Court A")).thenReturn(false)
        `when`(courtMapper.toEntity(request)).thenReturn(court)
        `when`(courtRepository.save(anyNonNull())).thenReturn(court)
        `when`(courtMapper.toResponse(court)).thenReturn(sampleResponse())

        val result = courtService.create(request)

        assertEquals("Court A", result.name)
        verify(courtRepository).save(court)
    }

    @Test
    fun `create throws DuplicateCourtNameException when name exists`() {
        val request = sampleRequest()
        `when`(courtRepository.existsByName("Court A")).thenReturn(true)

        assertThrows<DuplicateCourtNameException> {
            courtService.create(request)
        }
        verify(courtRepository, never()).save(anyNonNull())
    }

    @Test
    fun `update modifies court when exists`() {
        val court = sampleCourt()
        val request = sampleRequest(name = "Court Updated")
        val updatedCourt = sampleCourt(name = "Court Updated")

        `when`(courtRepository.findById(1L)).thenReturn(Optional.of(court))
        `when`(courtRepository.existsByName("Court Updated")).thenReturn(false)
        `when`(courtRepository.save(court)).thenReturn(updatedCourt)
        `when`(courtMapper.toResponse(updatedCourt)).thenReturn(sampleResponse(name = "Court Updated"))

        val result = courtService.update(1L, request)

        assertEquals("Court Updated", result.name)
    }

    @Test
    fun `delete removes court when exists`() {
        `when`(courtRepository.existsById(1L)).thenReturn(true)

        courtService.delete(1L)

        verify(courtRepository).deleteById(1L)
    }
}