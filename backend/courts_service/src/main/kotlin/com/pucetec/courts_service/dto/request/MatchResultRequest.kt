package com.pucetec.courts_service.dto.request

import com.pucetec.courts_service.entities.MatchResult
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class MatchResultRequest(
    @field:NotNull(message = "Status is required")
    val status: MatchResult.MatchStatus,

    @field:Valid
    @field:Size(min = 2, max = 2, message = "Exactly 2 teams must be provided")
    val teams: List<TeamScoreRequest>,

    val winner: String? = null,

    val playedAt: LocalDateTime? = null
)

data class TeamScoreRequest(
    @field:NotBlank(message = "Team name is required")
    @field:Size(max = 100)
    val name: String,

    @field:NotNull(message = "Score is required")
    @field:Min(value = 0, message = "Score cannot be negative")
    val score: Int
)