package com.pucetec.courts_service.repositories

import com.pucetec.courts_service.entities.Court
import org.springframework.data.jpa.repository.JpaRepository

interface CourtRepository : JpaRepository<Court, Long> {
    fun existsByName(name: String): Boolean
}