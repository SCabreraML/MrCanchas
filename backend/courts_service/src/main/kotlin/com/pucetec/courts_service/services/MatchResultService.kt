package com.pucetec.courts_service.services

import com.pucetec.courts_service.dto.request.MatchResultRequest
import com.pucetec.courts_service.dto.response.MatchResultResponse
import com.pucetec.courts_service.entities.MatchResult
import com.pucetec.courts_service.entities.Reservation
import com.pucetec.courts_service.exceptions.InvalidTeamsException
import com.pucetec.courts_service.exceptions.InvalidWinnerException
import com.pucetec.courts_service.exceptions.MatchResultAlreadyExistsException
import com.pucetec.courts_service.exceptions.MatchResultNotFoundException
import com.pucetec.courts_service.exceptions.ReservationNotFoundException
import com.pucetec.courts_service.mappers.MatchResultMapper
import com.pucetec.courts_service.repositories.MatchResultRepository
import com.pucetec.courts_service.repositories.ReservationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MatchResultService(
    private val matchResultRepository: MatchResultRepository,
    private val reservationRepository: ReservationRepository,
    private val matchResultMapper: MatchResultMapper
) {

    @Transactional
    fun create(reservationId: Long, request: MatchResultRequest): MatchResultResponse {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { ReservationNotFoundException(reservationId) }

        matchResultRepository.findByReservationId(reservationId)?.let {
            throw MatchResultAlreadyExistsException(reservationId)
        }

        // Ahora los equipos vienen en una lista (validada a exactamente 2 en el DTO)
        val teamA = request.teams[0]
        val teamB = request.teams[1]

        // Validar que los nombres de los equipos sean diferentes
        if (teamA.name.trim().lowercase() == teamB.name.trim().lowercase()) {
            throw InvalidTeamsException("The two teams cannot have the same name")
        }

        // Validar winner (si se envía)
        if (!request.winner.isNullOrBlank()) {
            val teamNames = listOf(teamA.name, teamB.name)
            if (request.winner !in teamNames) {
                throw InvalidWinnerException("Winner must be one of the two teams: $teamNames")
            }
        }

        val matchResult = MatchResult(
            reservation = reservation,
            status = request.status,
            teamA = teamA.name,
            scoreA = teamA.score,
            teamB = teamB.name,
            scoreB = teamB.score,
            winner = request.winner,
            playedAt = request.playedAt ?: LocalDateTime.now()
        )

        reservation.status = Reservation.Status.COMPLETED
        reservationRepository.save(reservation)

        val saved = matchResultRepository.save(matchResult)
        return matchResultMapper.toResponse(saved)
    }

    fun findByReservation(reservationId: Long): MatchResultResponse {
        val result = matchResultRepository.findByReservationId(reservationId)
            ?: throw MatchResultNotFoundException(reservationId)
        return matchResultMapper.toResponse(result)
    }
}