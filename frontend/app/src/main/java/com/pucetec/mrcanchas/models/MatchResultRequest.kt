package com.pucetec.mrcanchas.models

data class TeamScoreRequest(
    val name: String,
    val score: Int
)

data class MatchResultRequest(
    val teams: List<TeamScoreRequest>,
    val winner: String?,
    val status: String,
    val playedAt: String
)