package com.pucetec.courts_service.controllers

import com.pucetec.courts_service.dto.request.CourtRequest
import com.pucetec.courts_service.dto.response.CourtResponse
import com.pucetec.courts_service.services.CourtService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/courts")
class CourtController(
    private val courtService: CourtService
) {

    @GetMapping
    fun findAll(): ResponseEntity<List<CourtResponse>> =
        ResponseEntity.ok(courtService.findAll())

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: Long
    ): ResponseEntity<CourtResponse> =
        ResponseEntity.ok(courtService.findById(id))

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(
        @Valid @RequestBody request: CourtRequest
    ): ResponseEntity<CourtResponse> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(courtService.create(request))

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: CourtRequest
    ): ResponseEntity<CourtResponse> =
        ResponseEntity.ok(
            courtService.update(id, request)
        )

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        courtService.delete(id)

        return ResponseEntity.noContent().build()
    }
}