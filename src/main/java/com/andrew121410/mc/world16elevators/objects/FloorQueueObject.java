package com.andrew121410.mc.world16elevators.objects;

import java.util.Objects;

public class FloorQueueObject {

    private int floorNumber;
    private ElevatorStatus elevatorStatus;

    public FloorQueueObject(int floorNumber, ElevatorStatus elevatorStatus) {
        this.floorNumber = floorNumber;
        this.elevatorStatus = elevatorStatus;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public ElevatorStatus getElevatorStatus() {
        return elevatorStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FloorQueueObject that = (FloorQueueObject) o;
        return floorNumber == that.floorNumber &&
                elevatorStatus == that.elevatorStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(floorNumber, elevatorStatus);
    }
}