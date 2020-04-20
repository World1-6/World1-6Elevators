package com.andrew121410.mc.world16elevators.objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode
@ToString
@Getter
@SerializableAs("FloorObject")
public class FloorObject implements ConfigurationSerializable {

    private int floor;
    private Location mainDoor;
    private List<Location> doorList;
    private List<SignObject> signList;

    public FloorObject(int floor, Location mainDoor) {
        this(floor, mainDoor, new ArrayList<>(), new ArrayList<>());
    }

    public FloorObject(int floor, Location mainDoor, List<Location> doorList, List<SignObject> signList) {
        this.floor = floor;
        this.mainDoor = mainDoor;
        this.doorList = doorList;
        this.signList = signList;
    }

    public static FloorObject from(ElevatorMovement elevatorMovement) {
        return new FloorObject(elevatorMovement.getFloor(), elevatorMovement.getAtDoor());
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("Floor", floor);
        map.put("MainDoor", mainDoor);
        map.put("DoorList", doorList);
        map.put("SignList", signList);
        return map;
    }

    public static FloorObject deserialize(Map<String, Object> map) {
        return new FloorObject((int) map.get("Floor"), (Location) map.get("MainDoor"), (List<Location>) map.get("DoorList"), (List<SignObject>) map.get("SignList"));
    }
}