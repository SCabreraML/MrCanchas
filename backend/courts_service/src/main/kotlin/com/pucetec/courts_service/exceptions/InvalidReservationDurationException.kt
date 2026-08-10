package com.pucetec.courts_service.exceptions

class InvalidReservationDurationException(hours: Long) :
    RuntimeException("Reservation duration must be between 1 and 7 hours (current: $hours hours)")