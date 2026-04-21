package com.smartcampus;

import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;

/**
 * Resource controller for Room operations.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    /**
     * GET /api/v1/rooms
     * Retrieves all registered rooms.
     */
    @GET
    public Response getAllRooms() {
        Collection<Room> roomList = DataStore.rooms.values();
        return Response.ok(roomList).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Retrieves a specific room by its distinct ID.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            throw new WebApplicationException(
                Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Not Found\", \"message\": \"Room " + roomId + " does not exist.\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
            );
        }
        return Response.ok(room).build();
    }

    /**
     * POST /api/v1/rooms
     * Registers a new room into the system.
     */
    @POST
    public Response createRoom(Room newRoom) {
        // Validation: Require explicit ID
        if (newRoom.getId() == null || newRoom.getId().trim().isEmpty()) {
            throw new WebApplicationException(
                Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Bad Request\", \"message\": \"Room requires a valid textual id (e.g., 'LIB-301').\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
            );
        }

        // Validation: Check for uniqueness
        if (DataStore.rooms.containsKey(newRoom.getId())) {
             throw new WebApplicationException(
                Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Conflict\", \"message\": \"A room with id " + newRoom.getId() + " already exists.\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
            );
        }

        // Add to main memory pool
        DataStore.rooms.put(newRoom.getId(), newRoom);

        // Return a successful 201 Created and return the newly generated object
        return Response.status(Response.Status.CREATED).entity(newRoom).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Safely deletes a room if there are no existing sensors deployed inside it.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);

        // Standard 404 if the room never existed or was already deleted gracefully handling Idempotency
        if (room == null) {
            throw new WebApplicationException(
                Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Not Found\", \"message\": \"Room " + roomId + " does not exist.\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
            );
        }

        // Logical Constraint: Prevent Data Orphans
        if (!room.getSensorIds().isEmpty()) {
            throw new WebApplicationException(
                Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"Conflict\", \"message\": \"Cannot delete room " + roomId + " because it still contains active sensors.\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
            );
        }

        // Execute Deletion
        DataStore.rooms.remove(roomId);

        // Return a meaningful JSON representation of the deletion
        String successJson = String.format("{\"status\": \"success\", \"message\": \"Room %s deleted successfully\"}", roomId);
        return Response.ok(successJson).build();
    }
}

