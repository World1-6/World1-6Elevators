package com.andrew121410.mc.world16elevators.objects;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SerializableAs("ElevatorMovement")
public class ElevatorMovement implements ConfigurationSerializable {

    private Integer floor;

    private Location atDoor;
    private Location locationDOWN;
    private Location locationUP;

    //Config
    private long ticksPerSecond;
    private long doorHolderTicksPerSecond;
    private long elevatorWaiterTicksPerSecond;

    public static final long DEFAULT_TICKS_PER_SECOND = 6L;
    public static final long DEFAULT_DOOR_HOLDER_TICKS_PER_SECOND = 20L * 5L;
    public static final long DEFAULT_ELEVATOR_WAITER_TICKS_PER_SECOND = 20L * 6L;
    //...

    public ElevatorMovement(Integer floor, Location atDoor, Location locationDOWN, Location locationUP) {
        this(floor, atDoor, locationDOWN, locationUP, DEFAULT_TICKS_PER_SECOND, DEFAULT_DOOR_HOLDER_TICKS_PER_SECOND, DEFAULT_ELEVATOR_WAITER_TICKS_PER_SECOND);
    }

    public ElevatorMovement(Integer floor, Location atDoor, Location locationDOWN, Location locationUP, long ticksPerSecond, long doorHolderTicksPerSecond, long elevatorWaiterTicksPerSecond) {
        this.floor = floor;
        this.atDoor = atDoor;
        this.locationDOWN = locationDOWN;
        this.locationUP = locationUP;
        this.ticksPerSecond = ticksPerSecond;
        this.doorHolderTicksPerSecond = doorHolderTicksPerSecond;
        this.elevatorWaiterTicksPerSecond = elevatorWaiterTicksPerSecond;
    }

    public void moveUP() {
        this.atDoor.add(0, 1, 0);
        this.locationUP.add(0, 1, 0);
        this.locationDOWN.add(0, 1, 0);
    }

    public void moveDOWN() {
        this.atDoor.subtract(0, 1, 0);
        this.locationUP.subtract(0, 1, 0);
        this.locationDOWN.subtract(0, 1, 0);
    }

    //GETTERS AND SETTERS
    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Location getAtDoor() {
        return atDoor;
    }

    public void setAtDoor(Location atDoor) {
        this.atDoor = atDoor;
    }

    public Location getLocationDOWN() {
        return locationDOWN;
    }

    public void setLocationDOWN(Location locationDOWN) {
        this.locationDOWN = locationDOWN;
    }

    public Location getLocationUP() {
        return locationUP;
    }

    public void setLocationUP(Location locationUP) {
        this.locationUP = locationUP;
    }

    public long getTicksPerSecond() {
        return ticksPerSecond;
    }

    public void setTicksPerSecond(long ticksPerSecond) {
        this.ticksPerSecond = ticksPerSecond;
    }

    public long getDoorHolderTicksPerSecond() {
        return doorHolderTicksPerSecond;
    }

    public void setDoorHolderTicksPerSecond(long doorHolderTicksPerSecond) {
        this.doorHolderTicksPerSecond = doorHolderTicksPerSecond;
    }

    public long getElevatorWaiterTicksPerSecond() {
        return elevatorWaiterTicksPerSecond;
    }

    public void setElevatorWaiterTicksPerSecond(long elevatorWaiterTicksPerSecond) {
        this.elevatorWaiterTicksPerSecond = elevatorWaiterTicksPerSecond;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("floor", floor);
        map.put("atDoor", atDoor);
        map.put("locationDOWN", this.locationUP);
        map.put("locationUP", this.locationDOWN);
        map.put("ticksPerSecond", this.ticksPerSecond);
        map.put("doorHolderTicksPerSecond", this.doorHolderTicksPerSecond);
        map.put("elevatorWaiterTicksPerSecond", this.elevatorWaiterTicksPerSecond);
        return map;
    }

    public static ElevatorMovement deserialize(Map<String, Object> map) {
        return new ElevatorMovement((Integer) map.get("floor"), (Location) map.get("atDoor"), (Location) map.get("locationDOWN"), (Location) map.get("locationUP"), ((Integer) map.get("ticksPerSecond")).longValue(), ((Integer) map.get("doorHolderTicksPerSecond")).longValue(), ((Integer) map.get("elevatorWaiterTicksPerSecond")).longValue());
    }

    //JAVA
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElevatorMovement that = (ElevatorMovement) o;
        return ticksPerSecond == that.ticksPerSecond &&
                doorHolderTicksPerSecond == that.doorHolderTicksPerSecond &&
                elevatorWaiterTicksPerSecond == that.elevatorWaiterTicksPerSecond &&
                Objects.equals(floor, that.floor) &&
                Objects.equals(atDoor, that.atDoor) &&
                Objects.equals(locationDOWN, that.locationDOWN) &&
                Objects.equals(locationUP, that.locationUP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(floor, atDoor, locationDOWN, locationUP, ticksPerSecond, doorHolderTicksPerSecond, elevatorWaiterTicksPerSecond);
    }

    @Override
    public String toString() {
        return "ElevatorMovement{" +
                "floor=" + floor +
                ", atDoor=" + atDoor +
                ", locationDOWN=" + locationDOWN +
                ", locationUP=" + locationUP +
                ", ticksPerSecond=" + ticksPerSecond +
                ", doorHolderTicksPerSecond=" + doorHolderTicksPerSecond +
                ", elevatorWaiterTicksPerSecond=" + elevatorWaiterTicksPerSecond +
                '}';
    }
}