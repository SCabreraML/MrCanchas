package com.pucetec.courts_service.dto.response

import java.time.LocalDate
import java.time.LocalTime

data class TimeSlotResponse(
    val id: Long,
    val courtId: Long,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: String
)
