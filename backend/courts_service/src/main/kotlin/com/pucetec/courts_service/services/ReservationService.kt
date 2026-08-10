package com.pucetec.courts_service.services

import com.pucetec.courts_service.dto.request.ReservationRequest
import com.pucetec.courts_service.dto.response.ReservationResponse
import com.pucetec.courts_service.entities.Reservation
import com.pucetec.courts_service.entities.TimeSlot
import com.pucetec.courts_service.exceptions.InvalidReservationDurationException
import com.pucetec.courts_service.exceptions.ReservationInThePastException
import com.pucetec.courts_service.exceptions.ReservationNotFoundException
import com.pucetec.courts_service.exceptions.ReservationOutsideAllowedHoursException
import com.pucetec.courts_service.exceptions.TimeSlotAlreadyReservedException
import com.pucetec.courts_service.exceptions.TimeSlotNotFoundException
import com.pucetec.courts_service.exceptions.UnauthorizedReservationException
import com.pucetec.courts_service.mappers.ReservationMapper
import com.pucetec.courts_service.repositories.ReservationRepository
import com.pucetec.courts_service.repositories.TimeSlotRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val timeSlotRepository: TimeSlotRepository,
    private val reservationMapper: ReservationMapper
) {

    private val log = LoggerFactory.getLogger(ReservationService::class.java)

    private fun currentUsername(): String =
        SecurityContextHolder
            .getContext()
            .authentication?.name ?: throw IllegalStateException("No authenticated user found")

    @Transactional
    fun create(request: ReservationRequest): ReservationResponse {

        val timeSlot = timeSlotRepository.findById(request.timeSlotId)
            .orElseThrow { TimeSlotNotFoundException(request.timeSlotId) }

        val startDateTime = LocalDateTime.of(timeSlot.date, timeSlot.startTime)
        val endDateTime   = LocalDateTime.of(timeSlot.date, timeSlot.endTime)

        // ===== Validaciones de negocio =====

        // 1. No reservar en el pasado
        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw ReservationInThePastException()
        }

        // 2. Duración entre 1 y 7 horas
        val hours = java.time.Duration.between(startDateTime, endDateTime).toHours()
        if (hours < 1 || hours > 7) {
            throw InvalidReservationDurationException(hours)
        }

        // 3. Solo entre 07:00 y 20:00
        val allowedStart = LocalTime.of(7, 0)
        val allowedEnd   = LocalTime.of(20, 0)

        if (timeSlot.startTime.isBefore(allowedStart) || timeSlot.endTime.isAfter(allowedEnd)) {
            throw ReservationOutsideAllowedHoursException()
        }

        // 4. El time slot no debe estar ya reservado
        if (reservationRepository.existsByTimeSlotIdAndStatus(
                timeSlot.id!!,
                Reservation.Status.CONFIRMED
            )
        ) {
            throw TimeSlotAlreadyReservedException(timeSlot.id!!)
        }

        // ===================================

        timeSlot.status = TimeSlot.Status.RESERVED

        val reservation = Reservation(
            timeSlot = timeSlot,
            ownerUser = currentUsername(),
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            status = Reservation.Status.CONFIRMED
        )

        val saved = reservationRepository.save(reservation)
        timeSlotRepository.save(timeSlot)

        log.info("event=reservation.created | reservationId=${saved.id}")

        return reservationMapper.toResponse(saved)
    }

    fun findMyReservations(): List<ReservationResponse> =
        reservationRepository
            .findByOwnerUser(currentUsername())
            .map(reservationMapper::toResponse)

    fun findById(id: Long): ReservationResponse =
        reservationRepository.findById(id)
            .map(reservationMapper::toResponse)
            .orElseThrow {
                ReservationNotFoundException(id)
            }

    @Transactional
    fun cancel(id: Long) {

        val reservation = reservationRepository
            .findById(id)
            .orElseThrow {
                ReservationNotFoundException(id)
            }

        if (reservation.ownerUser != currentUsername()) {
            throw UnauthorizedReservationException()
        }

        reservation.status = Reservation.Status.CANCELLED

        reservation.timeSlot.status =
            TimeSlot.Status.AVAILABLE

        reservationRepository.save(reservation)
        timeSlotRepository.save(reservation.timeSlot)

        log.info("event=reservation.cancelled | msg=Reservation cancelled | reservationId=${reservation.id}")
    }
}