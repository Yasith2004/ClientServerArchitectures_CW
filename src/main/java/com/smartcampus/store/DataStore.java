package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe Data Store memory structures imitating a database.
 * The static maps ensure we maintain state across per-request JAX-RS Resource instantiations.
 */
public class DataStore {

    // Thread-safe map for Rooms. Key = Room ID
    public static final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();

    // Thread-safe map for Sensors. Key = Sensor ID
    public static final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Thread-safe map for SensorReadings. Key = Sensor ID, Value = Thread-safe List of Readings
    public static final ConcurrentHashMap<String, CopyOnWriteArrayList<SensorReading>> sensorReadings = new ConcurrentHashMap<>();
    
    // Private constructor prevents instantiation
    private DataStore() {
    }
}
