package com.pucetec.courts_service.services

import com.pucetec.courts_service.dto.request.CourtRequest
import com.pucetec.courts_service.dto.response.CourtResponse
import com.pucetec.courts_service.exceptions.CourtNotFoundException
import com.pucetec.courts_service.mappers.CourtMapper
import com.pucetec.courts_service.repositories.CourtRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.pucetec.courts_service.exceptions.DuplicateCourtNameException

@Service
class CourtService(
    private val courtRepository: CourtRepository,
    private val courtMapper: CourtMapper
) {

    fun findAll(): List<CourtResponse> =
        courtRepository.findAll()
            .map(courtMapper::toResponse)

    fun findById(id: Long): CourtResponse =
        courtRepository.findById(id)
            .map(courtMapper::toResponse)
            .orElseThrow {
                CourtNotFoundException(id)
            }

    @Transactional
    fun create(request: CourtRequest): CourtResponse {
        if (courtRepository.existsByName(request.name)) {
            throw DuplicateCourtNameException(request.name)
        }
        val court = courtMapper.toEntity(request)
        return courtMapper.toResponse(
            courtRepository.save(court)
        )
    }
    @Transactional
    fun update(
        id: Long,
        request: CourtRequest
    ): CourtResponse {

        val court = courtRepository.findById(id)
            .orElseThrow {
                CourtNotFoundException(id)
            }

        court.name = request.name
        court.sport = request.sport
        court.location = request.location
        court.available = request.available

        return courtMapper.toResponse(
            courtRepository.save(court)
        )
    }

    @Transactional
    fun delete(id: Long) {

        if (!courtRepository.existsById(id)) {
            throw CourtNotFoundException(id)
        }

        courtRepository.deleteById(id)
    }
}