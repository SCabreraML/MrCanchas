package com.pucetec.mrcanchas.models

data class Reservation(
    val id: Long,
    val timeSlotId: Long,
    val ownerUser: String,
    val startDateTime: String,
    val endDateTime: String,
    val status: String,
    val createdAt: String
)