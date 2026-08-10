package com.pucetec.courts_service.exceptions

class CourtNotFoundException(id: Long) :
    RuntimeException("Court with id $id was not found")