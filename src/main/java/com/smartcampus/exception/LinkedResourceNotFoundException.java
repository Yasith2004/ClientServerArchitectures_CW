package com.smartcampus.exception;

/**
 * Thrown when a resource references a linked entity (e.g., a roomId) that
 * does not exist in the system — even though the request body is syntactically valid.
 * Mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundExceptionMapper.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
