package com.pucetec.courts_service.controllers

import com.pucetec.courts_service.dto.request.TimeSlotRequest
import com.pucetec.courts_service.dto.response.TimeSlotResponse
import com.pucetec.courts_service.services.TimeSlotService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/time-slots")
class TimeSlotController(
    private val timeSlotService: TimeSlotService
) {

    @GetMapping
    fun findAll(): ResponseEntity<List<TimeSlotResponse>> =
        ResponseEntity.ok(timeSlotService.findAll())

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: Long
    ): ResponseEntity<TimeSlotResponse> =
        ResponseEntity.ok(timeSlotService.findById(id))

    @GetMapping("/court/{courtId}")
    fun findByCourt(
        @PathVariable courtId: Long
    ): ResponseEntity<List<TimeSlotResponse>> =
        ResponseEntity.ok(
            timeSlotService.findByCourt(courtId)
        )

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(
        @Valid @RequestBody request: TimeSlotRequest
    ): ResponseEntity<TimeSlotResponse> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(timeSlotService.create(request))

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: TimeSlotRequest
    ): ResponseEntity<TimeSlotResponse> =
        ResponseEntity.ok(
            timeSlotService.update(id, request)
        )

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        timeSlotService.delete(id)

        return ResponseEntity.noContent().build()
    }
}