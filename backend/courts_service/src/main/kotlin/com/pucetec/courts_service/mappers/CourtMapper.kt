package com.pucetec.courts_service.mappers

import com.pucetec.courts_service.dto.request.CourtRequest
import com.pucetec.courts_service.dto.response.CourtResponse
import com.pucetec.courts_service.entities.Court
import org.springframework.stereotype.Component

@Component
class CourtMapper {

    fun toEntity(request: CourtRequest): Court =
        Court(
            name = request.name,
            sport = request.sport,
            location = request.location,
            available = request.available
        )

    fun toResponse(court: Court): CourtResponse =
        CourtResponse(
            id = court.id!!,
            name = court.name,
            sport = court.sport,
            location = court.location,
            available = court.available
        )
}