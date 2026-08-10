package com.pucetec.courts_service.dto.response

import java.time.LocalDateTime

data class MatchResultResponse(
    val id: Long,
    val reservationId: Long,
    val teamA: String,
    val teamB: String,
    val scoreA: Int,
    val scoreB: Int,
    val winner: String?,
    val playedAt: LocalDateTime
)
