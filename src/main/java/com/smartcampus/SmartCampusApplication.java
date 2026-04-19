package com.smartcampus;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Main Application configuration class.
 * Establishes the versioned entry point for the API at /api/v1.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {
    public SmartCampusApplication() {
        // Auto-discover any JAX-RS components in the com.smartcampus package
        packages("com.smartcampus");
    }
}
