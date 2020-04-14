package com.andrew121410.mc.world16elevators.objects;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.*;

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

    //GETTERS
    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public Location getMainDoor() {
        return mainDoor;
    }

    public void setMainDoor(Location mainDoor) {
        this.mainDoor = mainDoor;
    }

    public List<Location> getDoorList() {
        return doorList;
    }

    public List<SignObject> getSignList() {
        return signList;
    }

    public static FloorObject from(ElevatorMovement elevatorMovement) {
        return new FloorObject(elevatorMovement.getFloor(), elevatorMovement.getAtDoor());
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("floor", floor);
        map.put("mainDoor", mainDoor);
        map.put("doorList", doorList);
        map.put("signList", signList);
        return map;
    }

    public static FloorObject deserialize(Map<String, Object> map) {
        return new FloorObject((int) map.get("floor"), (Location) map.get("mainDoor"), (List<Location>) map.get("doorList"), (List<SignObject>) map.get("signList"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FloorObject that = (FloorObject) o;
        return floor == that.floor &&
                Objects.equals(mainDoor, that.mainDoor) &&
                Objects.equals(doorList, that.doorList) &&
                Objects.equals(signList, that.signList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(floor, mainDoor, doorList, signList);
    }

    @Override
    public String toString() {
        return "FloorObject{" +
                "floor=" + floor +
                ", mainDoor=" + mainDoor +
                ", doorList=" + doorList +
                ", signList=" + signList +
                '}';
    }
}