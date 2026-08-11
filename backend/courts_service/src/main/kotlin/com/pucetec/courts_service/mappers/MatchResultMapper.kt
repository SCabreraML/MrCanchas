package com.pucetec.courts_service.mappers

import com.pucetec.courts_service.dto.response.MatchResultResponse
import com.pucetec.courts_service.entities.MatchResult
import org.springframework.stereotype.Component

@Component
class MatchResultMapper {

    fun toResponse(
        result: MatchResult
    ): MatchResultResponse =
        MatchResultResponse(
            id = result.id!!,
            reservationId = result.reservation.id!!,
            status = result.status.name,
            teamA = result.teamA,
            teamB = result.teamB,
            scoreA = result.scoreA,
            scoreB = result.scoreB,
            winner = result.winner,
            playedAt = result.playedAt
        )
}