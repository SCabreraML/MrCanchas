package com.pucetec.courts_service.controllers

import com.pucetec.courts_service.dto.request.MatchResultRequest
import com.pucetec.courts_service.dto.response.MatchResultResponse
import com.pucetec.courts_service.services.MatchResultService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/match-results")
class MatchResultController(
    private val matchResultService: MatchResultService
) {

    @PostMapping("/reservation/{reservationId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun create(
        @PathVariable reservationId: Long,
        @Valid @RequestBody request: MatchResultRequest
    ): ResponseEntity<MatchResultResponse> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                matchResultService.create(
                    reservationId,
                    request
                )
            )

    @GetMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun findByReservation(
        @PathVariable reservationId: Long
    ): ResponseEntity<MatchResultResponse> =
        ResponseEntity.ok(
            matchResultService.findByReservation(
                reservationId
            )
        )
}