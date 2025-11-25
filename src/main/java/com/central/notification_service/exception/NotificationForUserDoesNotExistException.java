package com.central.notification_service.exception;

public class NotificationForUserDoesNotExistException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message that describes which user was not found
     */
    public NotificationForUserDoesNotExistException(String message) {
        super(message);
    }
}