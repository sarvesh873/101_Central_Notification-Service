package com.central.notification_service.exception;



import org.openapitools.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * Global exception handler for the application.
 * Centralizes exception handling across all @Controller components.
 * Converts exceptions into appropriate HTTP responses with standardized error formats.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors and invalid input parameters.
     *
     * @param ex the caught IllegalArgumentException
     * @return ResponseEntity with HTTP 400 Bad Request status and error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        Double errorCode = 400.01;
        String description = "Invalid Input please check the input parameters";
        String errorType = HttpStatus.BAD_REQUEST.getReasonPhrase();
        String errorMessage = ex.getMessage();
        return generateErrorResponse(errorCode, description, errorType, errorMessage, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles cases where a requested user cannot be found.
     *
     * @param ex the caught UserDoesNotExistException
     * @return ResponseEntity with HTTP 404 Not Found status and error details
     */
    @ExceptionHandler(NotificationForUserDoesNotExistException.class)
    public ResponseEntity<ErrorResponse> handleUserDoesNotExistException(NotificationForUserDoesNotExistException ex) {
        Double errorCode = 404.09;
        String description = "A record with the specified user details does not exist";
        String errorType = HttpStatus.NOT_FOUND.getReasonPhrase();
        String errorMessage = ex.getMessage();

        return generateErrorResponse(errorCode, description, errorType, errorMessage, HttpStatus.NOT_FOUND);
    }


    /**
     * Helper method to generate a standardized error response.
     *
     * @param errorCode the HTTP status code
     * @param description a brief description of the error type
     * @param errorType the HTTP status reason phrase
     * @param errorMessage detailed error message for debugging
     * @return ResponseEntity containing the error details
     */
    private ResponseEntity<ErrorResponse> generateErrorResponse(
            Double errorCode,
            String description,
            String errorType,
            String errorMessage,
            HttpStatus httpStatus) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(errorCode)
                .description(description)
                .errorType(errorType)
                .errorMessage(errorMessage)
                .build();

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
