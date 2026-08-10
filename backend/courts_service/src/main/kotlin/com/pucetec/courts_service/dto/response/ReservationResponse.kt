package com.pucetec.courts_service.dto.response

import java.time.LocalDateTime

data class ReservationResponse(
    val id: Long,
    val timeSlotId: Long,
    val ownerUser: String,
    val status: String,
    val createdAt: LocalDateTime
)
