package com.hospital.management.exceptions;

/**
 * Exception thrown when user is not authorized to perform an action.
 * Results in HTTP 403 Forbidden response.
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
}
