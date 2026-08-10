package com.pucetec.mrcanchas.models

data class CourtRequest(
    val name: String,
    val sport: String,
    val location: String,
    val available: Boolean = true
)