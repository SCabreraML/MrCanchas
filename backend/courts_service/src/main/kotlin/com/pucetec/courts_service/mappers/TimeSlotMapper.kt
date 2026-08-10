package com.pucetec.courts_service.mappers

import com.pucetec.courts_service.dto.request.TimeSlotRequest
import com.pucetec.courts_service.dto.response.TimeSlotResponse
import com.pucetec.courts_service.entities.Court
import com.pucetec.courts_service.entities.TimeSlot
import org.springframework.stereotype.Component

@Component
class TimeSlotMapper {

    fun toEntity(
        request: TimeSlotRequest,
        court: Court
    ): TimeSlot =
        TimeSlot(
            court = court,
            date = request.date,
            startTime = request.startTime,
            endTime = request.endTime,
            status = TimeSlot.Status.AVAILABLE
        )

    fun toResponse(timeSlot: TimeSlot): TimeSlotResponse =
        TimeSlotResponse(
            id = timeSlot.id!!,
            courtId = timeSlot.court.id!!,
            date = timeSlot.date,
            startTime = timeSlot.startTime,
            endTime = timeSlot.endTime,
            status = timeSlot.status.name
        )
}