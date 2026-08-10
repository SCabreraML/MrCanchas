package com.pucetec.courts_service.mappers

import com.pucetec.courts_service.dto.response.ReservationResponse
import com.pucetec.courts_service.entities.Reservation
import org.springframework.stereotype.Component

@Component
class ReservationMapper {

    fun toResponse(
        reservation: Reservation
    ): ReservationResponse =
        ReservationResponse(
            id = reservation.id!!,
            timeSlotId = reservation.timeSlot.id!!,
            ownerUser = reservation.ownerUser,
            status = reservation.status.name,
            createdAt = reservation.createdAt
        )
}