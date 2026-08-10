package com.pucetec.courts_service.dto.request

import jakarta.validation.constraints.NotNull

data class ReservationRequest(
    @field:NotNull(message = "Time slot ID is required")
    val timeSlotId: Long
)
