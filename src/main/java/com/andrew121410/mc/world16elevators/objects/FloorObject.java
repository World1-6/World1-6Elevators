package com.andrew121410.mc.world16elevators.objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
@Setter
@SerializableAs("FloorObject")
public class FloorObject implements ConfigurationSerializable {

    private int floor;
    private String name;
    private Location mainDoor;
    private List<Location> doorList;
    private List<SignObject> signList;

    public FloorObject(int floor, String name, Location mainDoor, List<Location> doorList, List<SignObject> signList) {
        this.floor = floor;
        this.name = name;
        this.mainDoor = mainDoor;
        this.doorList = doorList;
        this.signList = signList;
    }

    public FloorObject(int floor, String name, Location mainDoor) {
        this(floor, name, mainDoor, new ArrayList<>(), new ArrayList<>());
    }

    public FloorObject(int floor, Location mainDoor) {
        this(floor, null, mainDoor, new ArrayList<>(), new ArrayList<>());
    }

    public FloorObject(String name, Location mainDoor) {
        this(Integer.MIN_VALUE, name, mainDoor, new ArrayList<>(), new ArrayList<>());
    }

    //Do not remove unnecessary bounding and .clone().
    public static FloorObject from(ElevatorMovement elevatorMovement) {
        return new FloorObject(new Integer(elevatorMovement.getFloor()), elevatorMovement.getAtDoor().clone());
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("Floor", this.floor);
        map.put("Name", this.name);
        map.put("MainDoor", this.mainDoor);
        map.put("DoorList", this.doorList);
        map.put("SignList", this.signList);
        return map;
    }

    public static FloorObject deserialize(Map<String, Object> map) {
        return new FloorObject((int) map.get("Floor"), (String) map.get("Name"), (Location) map.get("MainDoor"), (List<Location>) map.get("DoorList"), (List<SignObject>) map.get("SignList"));
    }
}