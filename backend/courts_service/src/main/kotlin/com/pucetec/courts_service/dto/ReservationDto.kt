package com.pucetec.courts_service.dto

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class ReservationDto(

    val id: Long? = null,

    @field:NotNull(message = "Time slot ID is required")
    val timeSlotId: Long?,

    val ownerUser: String? = null,

    val status: String? = null,

    val createdAt: LocalDateTime? = null
)