package com.pucetec.courts_service.controllers

import com.pucetec.courts_service.dto.request.ReservationRequest
import com.pucetec.courts_service.dto.response.ReservationResponse
import com.pucetec.courts_service.services.ReservationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reservations")
class ReservationController(
    private val reservationService: ReservationService
) {

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    fun create(
        @Valid @RequestBody request: ReservationRequest
    ): ResponseEntity<ReservationResponse> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(reservationService.create(request))

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    fun findMine():
            ResponseEntity<List<ReservationResponse>> =
        ResponseEntity.ok(
            reservationService.findMyReservations()
        )

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun findById(
        @PathVariable id: Long
    ): ResponseEntity<ReservationResponse> =
        ResponseEntity.ok(
            reservationService.findById(id)
        )

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    fun cancel(
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        reservationService.cancel(id)

        return ResponseEntity.noContent().build()
    }
}