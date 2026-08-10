package com.pucetec.courts_service.exceptions

class DuplicateTimeSlotException(courtId: Long) :
    RuntimeException("A time slot with the same court, date and time already exists for court $courtId")