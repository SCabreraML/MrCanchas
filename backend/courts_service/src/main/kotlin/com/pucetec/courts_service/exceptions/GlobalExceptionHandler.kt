package com.pucetec.courts_service.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
    val status: Int,
    val message: String
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CourtNotFoundException::class)
    fun handleCourtNotFound(
        ex: CourtNotFoundException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(404, ex.message ?: "Court not found"))

    @ExceptionHandler(TimeSlotNotFoundException::class)
    fun handleTimeSlotNotFound(
        ex: TimeSlotNotFoundException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(404, ex.message ?: "Time slot not found"))

    @ExceptionHandler(ReservationNotFoundException::class)
    fun handleReservationNotFound(
        ex: ReservationNotFoundException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(404, ex.message ?: "Reservation not found"))

    @ExceptionHandler(MatchResultNotFoundException::class)
    fun handleMatchResultNotFound(
        ex: MatchResultNotFoundException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(404, ex.message ?: "Match result not found"))

    @ExceptionHandler(TimeSlotAlreadyReservedException::class)
    fun handleTimeSlotAlreadyReserved(
        ex: TimeSlotAlreadyReservedException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(409, ex.message ?: "Time slot already reserved"))

    @ExceptionHandler(UnauthorizedReservationException::class)
    fun handleUnauthorizedReservation(
        ex: UnauthorizedReservationException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(403, ex.message ?: "Forbidden"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {

        val message = ex.bindingResult
            .fieldErrors
            .joinToString(", ") {
                "${it.field}: ${it.defaultMessage}"
            }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(400, message))
    }
    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException::class)
    fun handleAccessDenied(
        ex: org.springframework.security.authorization.AuthorizationDeniedException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(403, "Access denied"))

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception
    ): ResponseEntity<ErrorResponse> {
        ex.printStackTrace()
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    500,
                    "Internal server error"
                )
            )
    }
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException::class)
    fun handleDataIntegrity(
        ex: org.springframework.dao.DataIntegrityViolationException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(409, "This reservation already has a registered result"))
    @ExceptionHandler(DuplicateTimeSlotException::class)
    fun handleDuplicateTimeSlot(
        ex: DuplicateTimeSlotException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(409, ex.message ?: "Time slot already exists"))
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(400, ex.message ?: "Invalid request"))
    @ExceptionHandler(DuplicateCourtNameException::class)
    fun handleDuplicateCourtName(
        ex: DuplicateCourtNameException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(409, ex.message ?: "Court name already exists"))
    @ExceptionHandler(ReservationInThePastException::class)
    fun handleReservationInThePast(
        ex: ReservationInThePastException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(400, ex.message ?: "Cannot create a reservation in the past"))

    @ExceptionHandler(InvalidReservationDurationException::class)
    fun handleInvalidDuration(
        ex: InvalidReservationDurationException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(400, ex.message ?: "Invalid reservation duration"))

    @ExceptionHandler(ReservationOutsideAllowedHoursException::class)
    fun handleOutsideHours(
        ex: ReservationOutsideAllowedHoursException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(400, ex.message ?: "Reservations are only allowed between 07:00 and 20:00"))

    @ExceptionHandler(InvalidTeamsException::class)
    fun handleInvalidTeams(
        ex: InvalidTeamsException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(400, ex.message ?: "Invalid teams"))

    @ExceptionHandler(InvalidWinnerException::class)
    fun handleInvalidWinner(
        ex: InvalidWinnerException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(400, ex.message ?: "Invalid winner"))

    @ExceptionHandler(MatchResultAlreadyExistsException::class)
    fun handleMatchResultAlreadyExists(
        ex: MatchResultAlreadyExistsException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(409, ex.message ?: "Match result already exists"))
}