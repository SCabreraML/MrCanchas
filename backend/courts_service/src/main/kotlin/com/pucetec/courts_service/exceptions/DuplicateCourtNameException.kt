package com.pucetec.courts_service.exceptions

class DuplicateCourtNameException(name: String) :
    RuntimeException("A court with name '$name' already exists")