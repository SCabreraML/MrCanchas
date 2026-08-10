package com.pucetec.courts_service.services

import com.pucetec.courts_service.dto.request.TimeSlotRequest
import com.pucetec.courts_service.dto.response.TimeSlotResponse
import com.pucetec.courts_service.entities.TimeSlot
import com.pucetec.courts_service.exceptions.CourtNotFoundException
import com.pucetec.courts_service.exceptions.DuplicateTimeSlotException
import com.pucetec.courts_service.exceptions.TimeSlotNotFoundException
import com.pucetec.courts_service.mappers.TimeSlotMapper
import com.pucetec.courts_service.repositories.CourtRepository
import com.pucetec.courts_service.repositories.TimeSlotRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TimeSlotService(
    private val timeSlotRepository: TimeSlotRepository,
    private val courtRepository: CourtRepository,
    private val timeSlotMapper: TimeSlotMapper
) {

    fun findAll(): List<TimeSlotResponse> =
        timeSlotRepository.findAll()
            .map(timeSlotMapper::toResponse)

    fun findByCourt(courtId: Long): List<TimeSlotResponse> {

        if (!courtRepository.existsById(courtId)) {
            throw CourtNotFoundException(courtId)
        }

        return timeSlotRepository
            .findByCourtId(courtId)
            .map(timeSlotMapper::toResponse)
    }

    fun findById(id: Long): TimeSlotResponse =
        timeSlotRepository.findById(id)
            .map(timeSlotMapper::toResponse)
            .orElseThrow {
                TimeSlotNotFoundException(id)
            }

    @Transactional
    fun create(request: TimeSlotRequest): TimeSlotResponse {

        val court = courtRepository.findById(request.courtId)
            .orElseThrow {
                CourtNotFoundException(request.courtId)
            }

        require(request.startTime.isBefore(request.endTime)) {
            "Start time must be before end time"
        }

        if (timeSlotRepository.existsByCourtIdAndDateAndStartTimeAndEndTime(
                request.courtId, request.date, request.startTime, request.endTime
            )
        ) {
            throw DuplicateTimeSlotException(request.courtId)
        }

        val timeSlot = timeSlotMapper.toEntity(
            request,
            court
        )

        return timeSlotMapper.toResponse(
            timeSlotRepository.save(timeSlot)
        )
    }

    @Transactional
    fun update(
        id: Long,
        request: TimeSlotRequest
    ): TimeSlotResponse {

        val timeSlot = timeSlotRepository.findById(id)
            .orElseThrow {
                TimeSlotNotFoundException(id)
            }

        val court = courtRepository.findById(request.courtId)
            .orElseThrow {
                CourtNotFoundException(request.courtId)
            }

        require(request.startTime.isBefore(request.endTime)) {
            "Start time must be before end time"
        }

        timeSlot.court = court
        timeSlot.date = request.date
        timeSlot.startTime = request.startTime
        timeSlot.endTime = request.endTime

        return timeSlotMapper.toResponse(
            timeSlotRepository.save(timeSlot)
        )
    }

    @Transactional
    fun delete(id: Long) {

        if (!timeSlotRepository.existsById(id)) {
            throw TimeSlotNotFoundException(id)
        }

        timeSlotRepository.deleteById(id)
    }
}