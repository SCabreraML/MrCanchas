package com.pucetec.courts_service.dto.response

data class CourtResponse(
    val id: Long,
    val name: String,
    val sport: String,
    val location: String,
    val available: Boolean
)
