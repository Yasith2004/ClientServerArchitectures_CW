package com.smartcampus.provider;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        ErrorResponse errorResponse = new ErrorResponse("Unprocessable Entity", exception.getMessage(), 422);
        return Response.status(422) // Using 422 Unprocessable Entity directly as it might not be in standard JAX-RS Status enum without extending
                .entity(errorResponse)
                .type("application/json")
                .build();
    }
}
