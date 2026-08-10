package com.pucetec.mrcanchas.models

data class Court(
    val id: Long,
    val name: String,
    val sport: String,
    val location: String,
    val available: Boolean
)