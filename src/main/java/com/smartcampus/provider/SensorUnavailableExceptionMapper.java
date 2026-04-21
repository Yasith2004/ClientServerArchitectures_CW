package com.smartcampus.provider;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        ErrorResponse errorResponse = new ErrorResponse("Forbidden", exception.getMessage(), 403);
        return Response.status(Response.Status.FORBIDDEN)
                .entity(errorResponse)
                .type("application/json")
                .build();
    }
}
