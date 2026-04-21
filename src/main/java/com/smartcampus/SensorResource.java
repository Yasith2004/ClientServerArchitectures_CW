package com.smartcampus;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resource controller for Sensor operations.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    /**
     * POST /api/v1/sensors
     * Registers a new sensor and links it to an existing Room.
     */
    @POST
    public Response createSensor(Sensor newSensor) {
        // Validation: Require explicit ID
        if (newSensor.getId() == null || newSensor.getId().trim().isEmpty()) {
            throw new WebApplicationException(
                Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Bad Request\", \"message\": \"Sensor requires a valid textual id (e.g., 'TEMP-001').\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
            );
        }

        // Validation: Check for uniqueness
        if (DataStore.sensors.containsKey(newSensor.getId())) {
             throw new WebApplicationException(
                Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Conflict\", \"message\": \"A sensor with id " + newSensor.getId() + " already exists.\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
            );
        }

        // Validation: Verify roomId exists
        if (newSensor.getRoomId() == null || newSensor.getRoomId().trim().isEmpty()) {
            throw new WebApplicationException(
                Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Bad Request\", \"message\": \"Sensor must define a valid roomId.\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
            );
        }

        Room existingRoom = DataStore.rooms.get(newSensor.getRoomId());
        if (existingRoom == null) {
            throw new WebApplicationException(
                Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Not Found\", \"message\": \"Parent Room " + newSensor.getRoomId() + " does not exist.\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
            );
        }

        // Default constraints for status mapping (e.g., ACTIVE if empty)
        if (newSensor.getStatus() == null || newSensor.getStatus().trim().isEmpty()) {
            newSensor.setStatus("ACTIVE");
        }

        // Add to main memory pool
        DataStore.sensors.put(newSensor.getId(), newSensor);

        // Link sensor ID to the respective Room
        synchronized (existingRoom) {
            if (!existingRoom.getSensorIds().contains(newSensor.getId())) {
                existingRoom.getSensorIds().add(newSensor.getId());
            }
        }

        // Return a successful 201 Created and return the newly generated object
        return Response.status(Response.Status.CREATED).entity(newSensor).build();
    }

    /**
     * GET /api/v1/sensors
     * Retrieves all registered sensors, optionally filtered by type.
     */
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        Collection<Sensor> allSensors = DataStore.sensors.values();

        if (type == null || type.trim().isEmpty()) {
            return Response.ok(allSensors).build();
        }

        // Filter by type
        List<Sensor> filteredSensors = allSensors.stream()
                .filter(sensor -> type.equalsIgnoreCase(sensor.getType()))
                .collect(Collectors.toList());

        return Response.ok(filteredSensors).build();
    }

    /**
     * Sub-Resource Locator Pattern:
     * Delegates requests for /api/v1/sensors/{sensorId}/readings to SensorReadingResource.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        // Validate if sensor actually exists
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            throw new WebApplicationException(
                Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Not Found\", \"message\": \"Sensor " + sensorId + " does not exist.\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
            );
        }

        // Delegate to the sub-resource controller
        return new SensorReadingResource(sensorId);
    }
}
