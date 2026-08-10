package com.pucetec.mrcanchas.models

data class TimeSlot(
    val id: Long,
    val courtId: Long,
    val date: String,
    val startTime: String,
    val endTime: String,
    val status: String
)