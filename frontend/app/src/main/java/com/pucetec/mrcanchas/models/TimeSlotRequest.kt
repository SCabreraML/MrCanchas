package com.pucetec.mrcanchas.models

data class TimeSlotRequest(
    val courtId: Long,
    val date: String,
    val startTime: String,
    val endTime: String
)