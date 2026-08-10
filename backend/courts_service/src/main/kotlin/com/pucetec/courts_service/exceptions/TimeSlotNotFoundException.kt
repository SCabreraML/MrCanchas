package com.pucetec.courts_service.exceptions

class TimeSlotNotFoundException(id: Long) :
    RuntimeException("Time slot with id $id was not found")