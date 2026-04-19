package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class.
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    // Note: Grizzly with Jersey handles the context root better when it is part of the BASE_URI
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config using our SmartCampusApplication mapping
        final ResourceConfig rc = new SmartCampusApplication();

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args
     */
    public static void main(String[] args) {
        try {
            startServer();
            Logger.getLogger(Main.class.getName()).info(String.format("Jersey app started with endpoints available at "
                    + "%s%nHit Ctrl-C to stop it...", BASE_URI));
                    
            // Keeping the main thread alive since Grizzly spans backgrounds threads
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
