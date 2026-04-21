package com.smartcampus.provider;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Filter for logging incoming requests and outgoing responses.
 * Implements observability as per Day 10 requirements.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());
    private static final String START_TIME = "request-start-time";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Mark the start time for latency calculation
        requestContext.setProperty(START_TIME, System.currentTimeMillis());

        LOGGER.info(String.format(">>> Incoming Request: %s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Long startTime = (Long) requestContext.getProperty(START_TIME);
        long latency = (startTime != null) ? (System.currentTimeMillis() - startTime) : 0;

        LOGGER.info(String.format("<<< Outgoing Response: %d %s (Latency: %dms)",
                responseContext.getStatus(),
                requestContext.getUriInfo().getRequestUri(),
                latency));
    }
}
