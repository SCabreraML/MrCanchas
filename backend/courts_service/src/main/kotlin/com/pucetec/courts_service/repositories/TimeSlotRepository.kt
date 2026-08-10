package com.pucetec.courts_service.repositories

import com.pucetec.courts_service.entities.TimeSlot
import org.springframework.data.jpa.repository.JpaRepository

interface TimeSlotRepository : JpaRepository<TimeSlot, Long> {

    fun findByCourtId(courtId: Long): List<TimeSlot>
    fun existsByCourtIdAndDateAndStartTimeAndEndTime(
        courtId: Long,
        date: java.time.LocalDate,
        startTime: java.time.LocalTime,
        endTime: java.time.LocalTime
    ): Boolean
}