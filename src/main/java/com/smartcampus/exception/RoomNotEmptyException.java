package com.smartcampus.exception;

/**
 * Thrown when a Room deletion is attempted but the room still has
 * active Sensors assigned to it.
 * Mapped to HTTP 409 Conflict by RoomNotEmptyExceptionMapper.
 */
public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String message) {
        super(message);
    }
}
