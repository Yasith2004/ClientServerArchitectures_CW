package com.smartcampus;

import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Sub-resource controller for SensorReadings.
 * Handled by the sub-resource locator in SensorResource.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /
     * Retrieves the history of all readings for the parent sensor.
     */
    @GET
    public Response getReadings() {
        var readings = DataStore.sensorReadings.getOrDefault(sensorId, new CopyOnWriteArrayList<>());
        return Response.ok(readings).build();
    }

    /**
     * POST /
     * Appends a new reading log to the parent sensor's history.
     * Side-effect: Updates the currentValue of the parent sensor.
     */
    @POST
    public Response createReading(SensorReading newReading) {
        // Validation: basic reading sanity check
        if (newReading == null) {
            throw new WebApplicationException(
                Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Bad Request\", \"message\": \"Reading body cannot be empty.\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
            );
        }

        // Auto-generate constraints if missing
        if (newReading.getId() == null || newReading.getId().trim().isEmpty()) {
            newReading.setId(UUID.randomUUID().toString());
        }
        if (newReading.getTimestamp() <= 0) {
            newReading.setTimestamp(System.currentTimeMillis());
        }

        // Add to main memory pool array
        DataStore.sensorReadings.putIfAbsent(sensorId, new CopyOnWriteArrayList<>());
        DataStore.sensorReadings.get(sensorId).add(newReading);

        // Side-effect: Update the parent Sensor's currentValue
        Sensor parentSensor = DataStore.sensors.get(sensorId);
        if (parentSensor != null) {
            parentSensor.setCurrentValue(newReading.getValue());
        }

        // Return a successful 201 Created and return the newly generated reading object
        return Response.status(Response.Status.CREATED).entity(newReading).build();
    }
}
