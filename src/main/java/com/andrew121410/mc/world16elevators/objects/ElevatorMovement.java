package com.andrew121410.mc.world16elevators.objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@SerializableAs("ElevatorMovement")
public class ElevatorMovement implements ConfigurationSerializable {

    private Integer floor;

    private Location atDoor;
    private Location locationDOWN;
    private Location locationUP;

    private boolean doElevatorLeveling;
    private boolean onlyTwoFloors;

    //Config
    private long ticksPerSecond;
    private long doorHolderTicksPerSecond;
    private long elevatorWaiterTicksPerSecond;

    public static final long DEFAULT_TICKS_PER_SECOND = 6L;
    public static final long DEFAULT_DOOR_HOLDER_TICKS_PER_SECOND = 20L * 5L;
    public static final long DEFAULT_ELEVATOR_WAITER_TICKS_PER_SECOND = 20L * 6L;
    //...

    public ElevatorMovement(Integer floor, Location atDoor, Location locationDOWN, Location locationUP) {
        this(floor, atDoor, locationDOWN, locationUP, DEFAULT_TICKS_PER_SECOND, DEFAULT_DOOR_HOLDER_TICKS_PER_SECOND, DEFAULT_ELEVATOR_WAITER_TICKS_PER_SECOND, true, false);
    }

    public ElevatorMovement(Integer floor, Location atDoor, Location locationDOWN, Location locationUP, long ticksPerSecond, long doorHolderTicksPerSecond, long elevatorWaiterTicksPerSecond, boolean doElevatorLeveling, boolean onlyTwoFloors) {
        this.floor = floor;
        this.atDoor = atDoor;
        this.locationDOWN = locationDOWN;
        this.locationUP = locationUP;
        this.ticksPerSecond = ticksPerSecond;
        this.doorHolderTicksPerSecond = doorHolderTicksPerSecond;
        this.elevatorWaiterTicksPerSecond = elevatorWaiterTicksPerSecond;
        this.doElevatorLeveling = doElevatorLeveling;
        this.onlyTwoFloors = onlyTwoFloors;
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

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("Floor", floor);
        map.put("AtDoor", atDoor);
        map.put("LocationDOWN", this.locationDOWN);
        map.put("LocationUP", this.locationUP);
        map.put("TicksPerSecond", this.ticksPerSecond);
        map.put("DoorHolderTicksPerSecond", this.doorHolderTicksPerSecond);
        map.put("ElevatorWaiterTicksPerSecond", this.elevatorWaiterTicksPerSecond);
        map.put("DoElevatorLeveling", this.doElevatorLeveling);
        map.put("OnlyTwoFloors", this.onlyTwoFloors);
        return map;
    }

    public static ElevatorMovement deserialize(Map<String, Object> map) {
        return new ElevatorMovement((Integer) map.get("Floor"),
                (Location) map.get("AtDoor"),
                (Location) map.get("LocationUP"),
                (Location) map.get("LocationDOWN"),
                ((Integer) map.get("TicksPerSecond")).longValue(),
                ((Integer) map.get("DoorHolderTicksPerSecond")).longValue(),
                ((Integer) map.get("ElevatorWaiterTicksPerSecond")).longValue(),
                (Boolean) map.get("DoElevatorLeveling"),
                (Boolean) map.get("OnlyTwoFloors"));
    }
}