package com.pucetec.courts_service.exceptions

class ReservationInThePastException (id: Long):
    RuntimeException("Cannot create a reservation in the past")
