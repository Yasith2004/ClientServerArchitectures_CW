package com.smartcampus.mapper;

import com.smartcampus.exception.RoomNotEmptyException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps RoomNotEmptyException to HTTP 409 Conflict.
 * Triggered when a client attempts to delete a Room that still has active Sensors.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        return Response
                .status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity("{\"error\": \"Conflict\", \"message\": \"" + exception.getMessage() + "\", \"status\": 409}")
                .build();
    }
}
