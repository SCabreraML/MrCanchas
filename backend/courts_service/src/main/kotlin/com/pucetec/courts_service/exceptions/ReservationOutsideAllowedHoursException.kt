package com.pucetec.courts_service.exceptions

class ReservationOutsideAllowedHoursException :
    RuntimeException("Reservations are only allowed between 07:00 and 20:00")