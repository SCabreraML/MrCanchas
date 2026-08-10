package com.pucetec.mrcanchas.models

data class MatchResult(
    val id: Long,
    val reservationId: Long,
    val status: String,
    val teamA: String,
    val teamB: String,
    val scoreA: Int,
    val scoreB: Int,
    val winner: String?,
    val playedAt: String
)