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

    public ElevatorMovement(Integer floor, Location atDoor, Location locationDOWN, Location locationUP) {
        this.floor = floor;
        this.atDoor = atDoor;
        this.locationDOWN = locationDOWN;
        this.locationUP = locationUP;
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
        map.put("Floor", this.floor);
        map.put("AtDoor", this.atDoor);
        map.put("LocationDOWN", this.locationDOWN);
        map.put("LocationUP", this.locationUP);
        return map;
    }

    public static ElevatorMovement deserialize(Map<String, Object> map) {
        return new ElevatorMovement((Integer) map.get("Floor"),
                (Location) map.get("AtDoor"),
                (Location) map.get("LocationUP"),
                (Location) map.get("LocationDOWN"));
    }
}