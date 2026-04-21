package com.smartcampus.mapper;

import com.smartcampus.exception.LinkedResourceNotFoundException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps LinkedResourceNotFoundException to HTTP 422 Unprocessable Entity.
 * Triggered when a POST body is syntactically valid JSON, but references
 * a linked entity (e.g., roomId) that does not exist in the system.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        return Response
                .status(422) // 422 Unprocessable Entity
                .type(MediaType.APPLICATION_JSON)
                .entity("{\"error\": \"Unprocessable Entity\", \"message\": \"" + exception.getMessage() + "\", \"status\": 422}")
                .build();
    }
}
