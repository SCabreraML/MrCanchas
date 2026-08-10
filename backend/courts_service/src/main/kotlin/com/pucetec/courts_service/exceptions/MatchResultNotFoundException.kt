package com.pucetec.courts_service.exceptions

class MatchResultNotFoundException(id: Long) :
    RuntimeException("Match result with id $id was not found")