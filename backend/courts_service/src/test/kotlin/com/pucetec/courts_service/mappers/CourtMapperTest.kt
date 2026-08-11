package com.pucetec.courts_service.mappers

import com.pucetec.courts_service.dto.request.CourtRequest
import com.pucetec.courts_service.entities.Court
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CourtMapperTest {

    private val mapper = CourtMapper()

    @Test
    fun `toEntity mapea todos los campos del request`() {
        val request = CourtRequest(
            name = "Cancha 1",
            sport = "Fútbol",
            location = "Complejo Norte",
            available = true
        )

        val entity = mapper.toEntity(request)

        assertEquals("Cancha 1", entity.name)
        assertEquals("Fútbol", entity.sport)
        assertEquals("Complejo Norte", entity.location)
        assertTrue(entity.available)
    }

    @Test
    fun `toResponse mapea todos los campos de la entidad`() {
        val court = Court(
            id = 5L,
            name = "Cancha 2",
            sport = "Tenis",
            location = "Complejo Sur",
            available = false
        )

        val response = mapper.toResponse(court)

        assertEquals(5L, response.id)
        assertEquals("Cancha 2", response.name)
        assertEquals("Tenis", response.sport)
        assertEquals("Complejo Sur", response.location)
        assertEquals(false, response.available)
    }
}