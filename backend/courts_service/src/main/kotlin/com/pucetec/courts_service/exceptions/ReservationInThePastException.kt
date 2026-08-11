package com.pucetec.courts_service.exceptions

class ReservationInThePastException :
    RuntimeException("Cannot create a reservation in the past")