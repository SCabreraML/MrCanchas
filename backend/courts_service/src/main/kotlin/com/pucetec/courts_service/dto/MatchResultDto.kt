package com.pucetec.courts_service.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class MatchResultDto(

    val id: Long? = null,

    val reservationId: Long? = null,

    @field:NotBlank(message = "Team A is required")
    @field:Size(max = 100)
    val teamA: String,

    @field:NotBlank(message = "Team B is required")
    @field:Size(max = 100)
    val teamB: String,

    @field:NotNull(message = "Score A is required")
    @field:Min(value = 0, message = "Score cannot be negative")
    val scoreA: Int?,

    @field:NotNull(message = "Score B is required")
    @field:Min(value = 0, message = "Score cannot be negative")
    val scoreB: Int?,

    val winner: String? = null,

    @field:NotNull(message = "Played at is required")
    val playedAt: LocalDateTime?
)