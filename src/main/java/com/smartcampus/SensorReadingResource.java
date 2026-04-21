package com.smartcampus;

import com.smartcampus.exception.SensorUnavailableException;
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
     * GET /api/v1/sensors/{sensorId}/readings
     * Retrieves the full history of readings for the parent sensor.
     */
    @GET
    public Response getReadings() {
        var readings = DataStore.sensorReadings.getOrDefault(sensorId, new CopyOnWriteArrayList<>());
        return Response.ok(readings).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading log to the parent sensor's history.
     *
     * Side-effect: Updates the currentValue of the parent Sensor object.
     *
     * Business Rule: A sensor in "MAINTENANCE" status cannot accept new readings.
     * Throws SensorUnavailableException (-> 403 Forbidden) if enforced.
     */
    @POST
    public Response createReading(SensorReading newReading) {
        // Guard: basic presence check
        if (newReading == null) {
            throw new WebApplicationException(
                Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Bad Request\", \"message\": \"Reading body cannot be empty.\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
            );
        }

        // Business Rule: Block readings for MAINTENANCE sensors
        Sensor parentSensor = DataStore.sensors.get(sensorId);
        if (parentSensor != null && "MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is currently under MAINTENANCE and cannot accept new readings."
            );
        }

        // Auto-generate id if not provided
        if (newReading.getId() == null || newReading.getId().trim().isEmpty()) {
            newReading.setId(UUID.randomUUID().toString());
        }
        // Auto-set epoch timestamp if not provided
        if (newReading.getTimestamp() <= 0) {
            newReading.setTimestamp(System.currentTimeMillis());
        }

        // Append to the readings history list for this sensor
        DataStore.sensorReadings.putIfAbsent(sensorId, new CopyOnWriteArrayList<>());
        DataStore.sensorReadings.get(sensorId).add(newReading);

        // Side-effect: Update the parent Sensor's currentValue with the latest reading
        if (parentSensor != null) {
            parentSensor.setCurrentValue(newReading.getValue());
        }

        // Return 201 Created with the persisted reading body
        return Response.status(Response.Status.CREATED).entity(newReading).build();
    }
}
