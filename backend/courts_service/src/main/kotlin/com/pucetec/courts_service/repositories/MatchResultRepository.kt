package com.pucetec.courts_service.repositories

import com.pucetec.courts_service.entities.MatchResult
import org.springframework.data.jpa.repository.JpaRepository

interface MatchResultRepository : JpaRepository<MatchResult, Long> {

    fun findByReservationId(reservationId: Long): MatchResult?
}