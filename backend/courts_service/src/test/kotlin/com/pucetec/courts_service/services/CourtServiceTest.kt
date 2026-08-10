package com.pucetec.courts_service.services
import com.pucetec.courts_service.exceptions.DuplicateCourtNameException
import com.pucetec.courts_service.dto.request.CourtRequest
import com.pucetec.courts_service.dto.response.CourtResponse
import com.pucetec.courts_service.entities.Court
import com.pucetec.courts_service.exceptions.CourtNotFoundException
import com.pucetec.courts_service.mappers.CourtMapper
import com.pucetec.courts_service.repositories.CourtRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CourtServiceTest {

    @Mock
    private lateinit var courtRepository: CourtRepository

    @Mock
    private lateinit var courtMapper: CourtMapper

    @InjectMocks
    private lateinit var courtService: CourtService

    private fun sampleCourt(id: Long = 1L) =
        Court(id = id, name = "Court A", sport = "Tennis", location = "Zone 1", available = true)

    private fun sampleResponse(id: Long = 1L) =
        CourtResponse(id = id, name = "Court A", sport = "Tennis", location = "Zone 1", available = true)

    private fun sampleRequest() =
        CourtRequest(name = "Court A", sport = "Tennis", location = "Zone 1", available = true)

    @Test
    fun `findAll returns mapped courts`() {
        val court1 = sampleCourt(1L)
        val court2 = sampleCourt(2L)
        `when`(courtRepository.findAll()).thenReturn(listOf(court1, court2))
        `when`(courtMapper.toResponse(court1)).thenReturn(sampleResponse(1L))
        `when`(courtMapper.toResponse(court2)).thenReturn(sampleResponse(2L))

        val result = courtService.findAll()

        assertEquals(2, result.size)
    }

    @Test
    fun `findById returns the court when it exists`() {
        val court = sampleCourt(3L)
        `when`(courtRepository.findById(3L)).thenReturn(Optional.of(court))
        `when`(courtMapper.toResponse(court)).thenReturn(sampleResponse(3L))

        val result = courtService.findById(3L)

        assertEquals(3L, result.id)
        assertEquals("Court A", result.name)
    }

    @Test
    fun `findById throws CourtNotFoundException when it does not exist`() {
        `when`(courtRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<CourtNotFoundException> {
            courtService.findById(99L)
        }
    }

    @Test
    fun `create saves and returns the court`() {
        val request = sampleRequest()
        val entity = sampleCourt(1L)
        val saved = sampleCourt(1L)

        `when`(courtRepository.existsByName(request.name)).thenReturn(false)
        `when`(courtMapper.toEntity(request)).thenReturn(entity)
        `when`(courtRepository.save(entity)).thenReturn(saved)
        `when`(courtMapper.toResponse(saved)).thenReturn(sampleResponse(1L))

        val result = courtService.create(request)

        assertEquals(1L, result.id)
        assertEquals("Court A", result.name)
    }

    @Test
    fun `update modifies and returns the court when it exists`() {
        val existing = sampleCourt(5L)
        val request = CourtRequest(name = "New Name", sport = "Padel", location = "Zone 2", available = false)
        val saved = Court(id = 5L, name = "New Name", sport = "Padel", location = "Zone 2", available = false)

        `when`(courtRepository.findById(5L)).thenReturn(Optional.of(existing))
        `when`(courtRepository.save(existing)).thenReturn(saved)
        `when`(courtMapper.toResponse(saved))
            .thenReturn(CourtResponse(5L, "New Name", "Padel", "Zone 2", false))

        val result = courtService.update(5L, request)

        assertEquals("New Name", result.name)
        assertEquals("Padel", result.sport)
        assertEquals(false, result.available)
    }

    @Test
    fun `update throws CourtNotFoundException when it does not exist`() {
        `when`(courtRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<CourtNotFoundException> {
            courtService.update(99L, sampleRequest())
        }
    }

    @Test
    fun `delete removes the court when it exists`() {
        `when`(courtRepository.existsById(1L)).thenReturn(true)

        courtService.delete(1L)

        verify(courtRepository).deleteById(1L)
    }

    @Test
    fun `delete throws CourtNotFoundException when it does not exist`() {
        `when`(courtRepository.existsById(99L)).thenReturn(false)

        assertThrows<CourtNotFoundException> {
            courtService.delete(99L)
        }
        verify(courtRepository, never()).deleteById(99L)
    }
    @Test
    fun `create throws DuplicateCourtNameException when the name already exists`() {
        val request = sampleRequest()
        `when`(courtRepository.existsByName(request.name)).thenReturn(true)

        assertThrows<DuplicateCourtNameException> {
            courtService.create(request)
        }
    }
}