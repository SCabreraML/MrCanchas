package com.pucetec.courts_service.exceptions

class MatchResultAlreadyExistsException(reservationId: Long) :
    RuntimeException("A match result already exists for reservation $reservationId")