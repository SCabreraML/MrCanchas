package com.pucetec.courts_service.exceptions

class UnauthorizedReservationException :
    RuntimeException("You are not authorized to modify this reservation")