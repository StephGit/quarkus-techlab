package ch.puzzle.consumer.entity;

import java.time.Instant;

public class SensorMeasurement {

    public Double data;
    public Instant time;

    public SensorMeasurement() {
    }

    public SensorMeasurement(Double data, Instant time) {
        this.data = data;
        this.time = time;
    }
}
