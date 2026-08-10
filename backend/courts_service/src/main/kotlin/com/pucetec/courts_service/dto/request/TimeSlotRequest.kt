package com.pucetec.courts_service.dto.request

import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime

data class TimeSlotRequest(
    @field:NotNull(message = "Court ID is required")
    val courtId: Long,

    @field:NotNull(message = "Date is required")
    val date: LocalDate,

    @field:NotNull(message = "Start time is required")
    val startTime: LocalTime,

    @field:NotNull(message = "End time is required")
    val endTime: LocalTime
)
