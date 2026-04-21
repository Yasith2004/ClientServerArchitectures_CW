package com.smartcampus.provider;

import com.smartcampus.model.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic Exception Mapper that catches all unhandled Throwable instances.
 * Maps them to an HTTP 500 Internal Server Error, hiding sensitive stack traces from clients.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the actual exception internally for debugging
        LOGGER.log(Level.SEVERE, "An unhandled error occurred in the Smart Campus API", exception);

        // Create a user-friendly error response
        ErrorResponse errorResponse = new ErrorResponse(
                "Internal Server Error",
                "An unexpected system error occurred",
                500
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .type("application/json")
                .build();
    }
}
