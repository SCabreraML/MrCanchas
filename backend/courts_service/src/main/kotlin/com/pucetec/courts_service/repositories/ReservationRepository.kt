package com.pucetec.courts_service.repositories

import com.pucetec.courts_service.entities.Reservation
import org.springframework.data.jpa.repository.JpaRepository

interface ReservationRepository : JpaRepository<Reservation, Long> {

    fun findByOwnerUser(ownerUser: String): List<Reservation>

    fun existsByTimeSlotIdAndStatus(
        timeSlotId: Long,
        status: Reservation.Status
    ): Boolean
}