package com.pucetec.courts_service.exceptions

class ReservationNotFoundException(id: Long) :
    RuntimeException("Reservation with id $id was not found")