package com.smartcampus.provider;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        ErrorResponse errorResponse = new ErrorResponse("Conflict", exception.getMessage(), 409);
        return Response.status(Response.Status.CONFLICT)
                .entity(errorResponse)
                .type("application/json")
                .build();
    }
}
