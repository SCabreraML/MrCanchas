package com.pucetec.courts_service.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CourtRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 100)
    val name: String,

    @field:NotBlank(message = "Sport is required")
    @field:Size(max = 40)
    val sport: String,

    @field:NotBlank(message = "Location is required")
    @field:Size(max = 100)
    val location: String,

    val available: Boolean = true
)
