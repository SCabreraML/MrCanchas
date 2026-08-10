package com.pucetec.courts_service.exceptions

class TimeSlotAlreadyReservedException(id: Long) :
    RuntimeException("Time slot with id $id is already reserved")