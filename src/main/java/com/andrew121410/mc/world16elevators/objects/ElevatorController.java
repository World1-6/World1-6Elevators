package com.andrew121410.mc.world16elevators.objects;

import com.andrew121410.mc.world16elevators.World16Elevators;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@SerializableAs("ElevatorController")
public class ElevatorController implements ConfigurationSerializable {

    private World16Elevators plugin;

    private String controllerName;
    private Location mainChunk;
    private Map<String, ElevatorObject> elevatorsMap;

    public ElevatorController(World16Elevators plugin, String controllerName, Location mainChunk, Map<String, ElevatorObject> elevatorsMap) {
        this.plugin = plugin;
        this.controllerName = controllerName;
        this.mainChunk = mainChunk;
        this.elevatorsMap = elevatorsMap;
        this.elevatorsMap.forEach((k, v) -> v.setElevatorControllerName(this.controllerName));
    }

    public ElevatorController(World16Elevators plugin, String controllerName) {
        this(plugin, controllerName, null, new HashMap<>());
    }

    public void callElevatorClosest(int floorNum, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        ElevatorObject elevatorObject = getClosestElevator(floorNum, true, elevatorStatus);
        elevatorObject.goToFloor(floorNum, elevatorStatus, elevatorWho);
    }

    public void callElevatorClosest(String floorName, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        ElevatorObject elevatorObject = getClosestElevator(floorName, true, elevatorStatus);
        elevatorObject.goToFloor(floorName, elevatorStatus, elevatorWho);
    }

    public void callAllElevators(int floorNum, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        this.elevatorsMap.forEach((k, v) -> v.goToFloor(floorNum, elevatorStatus, elevatorWho));
    }

    public ElevatorObject getClosestElevator(int floorNumber, boolean smart, ElevatorStatus elevatorStatus) {
        Optional<ElevatorObject> optionalElevatorObject = this.elevatorsMap.values().stream().filter(elevatorObject -> !elevatorObject.isGoing()).min(Comparator.comparingInt(i -> Math.abs(i.getElevatorMovement().getAtDoor().getBlockY() - i.getFloor(floorNumber).getMainDoor().getBlockY())));
        Optional<ElevatorObject> optionalElevatorObject1 = this.elevatorsMap.values().stream().filter(ElevatorObject::isGoing).filter(elevatorObject -> elevatorObject.getFloorBuffer().contains(floorNumber) && elevatorObject.getStopBy().toElevatorStatus() == elevatorStatus).findFirst();
        return optionalElevatorObject1.orElseGet(optionalElevatorObject::get);
    }

    public ElevatorObject getClosestElevator(String floorName, boolean smart, ElevatorStatus elevatorStatus) {
        Optional<ElevatorObject> optionalElevatorObject = this.elevatorsMap.values().stream().filter(elevatorObject -> !elevatorObject.isGoing()).min(Comparator.comparingInt(i -> Math.abs(i.getElevatorMovement().getAtDoor().getBlockY() - i.getFloor(floorName).getMainDoor().getBlockY())));
        Optional<ElevatorObject> optionalElevatorObject1 = this.elevatorsMap.values().stream().filter(ElevatorObject::isGoing).filter(elevatorObject -> elevatorObject.getFloorBuffer().contains(elevatorObject.getFloor(floorName).getFloor()) && elevatorObject.getStopBy().toElevatorStatus() == elevatorStatus).findFirst();
        return optionalElevatorObject1.orElseGet(optionalElevatorObject::get);
    }

    public ElevatorObject getElevator(String name) {
        return elevatorsMap.get(name.toLowerCase());
    }

    public void registerElevator(String name, ElevatorObject elevatorObject) {
        if (this.mainChunk == null) {
            Chunk chunk = elevatorObject.getFloor(1).getMainDoor().getChunk();
            this.mainChunk = new Location(chunk.getWorld(), chunk.getX(), 0, chunk.getZ());
        }
        elevatorObject.setElevatorControllerName(this.controllerName);
        this.elevatorsMap.putIfAbsent(name.toLowerCase(), elevatorObject);
    }

    public boolean isSingle() {
        return this.elevatorsMap.size() == 0;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("ControllerName", this.controllerName);
        map.put("MainChunk", this.mainChunk);
        map.put("ElevatorMap", this.elevatorsMap);
        return map;
    }

    public static ElevatorController deserialize(Map<String, Object> map) {
        return new ElevatorController(World16Elevators.getInstance(), (String) map.get("ControllerName"), (Location) map.get("MainChunk"), (Map<String, ElevatorObject>) map.get("ElevatorMap"));
    }
}
