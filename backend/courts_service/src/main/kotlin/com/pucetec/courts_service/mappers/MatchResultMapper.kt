package com.pucetec.courts_service.mappers

import com.pucetec.courts_service.dto.request.MatchResultRequest
import com.pucetec.courts_service.dto.response.MatchResultResponse
import com.pucetec.courts_service.entities.MatchResult
import com.pucetec.courts_service.entities.Reservation
import org.springframework.stereotype.Component

@Component
class MatchResultMapper {

    fun toEntity(
        request: MatchResultRequest,
        reservation: Reservation
    ): MatchResult =
        MatchResult(
            reservation = reservation,
            teamA = request.teamA,
            teamB = request.teamB,
            scoreA = request.scoreA,
            scoreB = request.scoreB,
            winner = request.winner,
            playedAt = request.playedAt
        )

    fun toResponse(
        result: MatchResult
    ): MatchResultResponse =
        MatchResultResponse(
            id = result.id!!,
            reservationId = result.reservation.id!!,
            teamA = result.teamA,
            teamB = result.teamB,
            scoreA = result.scoreA,
            scoreB = result.scoreB,
            winner = result.winner,
            playedAt = result.playedAt
        )
}