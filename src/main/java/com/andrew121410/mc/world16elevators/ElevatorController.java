package com.andrew121410.mc.world16elevators;

import com.andrew121410.mc.world16elevators.enums.ElevatorStatus;
import com.andrew121410.mc.world16elevators.enums.ElevatorWho;
import com.andrew121410.mc.world16utils.chat.Translate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

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

    public void callElevatorClosest(Player player, int floorNumber, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        ElevatorObject elevatorObject = getClosestElevator(floorNumber, true, elevatorStatus);
        if (elevatorObject == null) return;
        player.sendMessage(Translate.color("&e&oCalled the nearest elevator on the controller"));
        elevatorObject.goToFloor(player, floorNumber, elevatorStatus, elevatorWho);
    }

    public void callElevatorClosest(Player player, String floorName, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        ElevatorObject elevatorObject = getClosestElevator(floorName, true, elevatorStatus);
        if (elevatorObject == null) return;
        player.sendMessage(Translate.color("&e&oCalled the nearest elevator on the controller"));
        elevatorObject.goToFloor(player, floorName, elevatorStatus, elevatorWho);
    }

    public void callElevatorClosest(int floorNumber, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        ElevatorObject elevatorObject = getClosestElevator(floorNumber, true, elevatorStatus);
        if (elevatorObject == null) return;
        elevatorObject.goToFloor(floorNumber, elevatorStatus, elevatorWho);
    }

    public void callElevatorClosest(String floorName, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        ElevatorObject elevatorObject = getClosestElevator(floorName, true, elevatorStatus);
        if (elevatorObject == null) return;
        elevatorObject.goToFloor(floorName, elevatorStatus, elevatorWho);
    }

    public void callAllElevators(int floorNum, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        this.elevatorsMap.forEach((k, v) -> v.goToFloor(floorNum, elevatorStatus, elevatorWho));
    }

    public ElevatorObject getClosestElevator(int floorNumber, boolean smart, ElevatorStatus elevatorStatus) {
        Optional<ElevatorObject> optionalElevatorObject = this.elevatorsMap.values().stream().filter(elevatorObject -> !elevatorObject.isGoing()).min(Comparator.comparingInt(i -> Math.abs(i.getElevatorMovement().getAtDoor().getBlockY() - i.getFloor(floorNumber).getBlockUnderMainDoor().getBlockY())));
        Optional<ElevatorObject> optionalElevatorObject1 = this.elevatorsMap.values().stream().filter(ElevatorObject::isGoing).filter(elevatorObject -> elevatorObject.getFloorBuffer().contains(floorNumber) && elevatorObject.getStopBy().toElevatorStatus() == elevatorStatus).findFirst();
        return optionalElevatorObject1.orElse(optionalElevatorObject.orElse(null));
    }

    public ElevatorObject getClosestElevator(String floorName, boolean smart, ElevatorStatus elevatorStatus) {
        Optional<ElevatorObject> optionalElevatorObject = this.elevatorsMap.values().stream().filter(elevatorObject -> !elevatorObject.isGoing()).min(Comparator.comparingInt(i -> Math.abs(i.getElevatorMovement().getAtDoor().getBlockY() - i.getFloor(floorName).getBlockUnderMainDoor().getBlockY())));
        Optional<ElevatorObject> optionalElevatorObject1 = this.elevatorsMap.values().stream().filter(ElevatorObject::isGoing).filter(elevatorObject -> elevatorObject.getFloorBuffer().contains(elevatorObject.getFloor(floorName).getFloor()) && elevatorObject.getStopBy().toElevatorStatus() == elevatorStatus).findFirst();
        return optionalElevatorObject1.orElse(optionalElevatorObject.orElse(null));
    }

    public ElevatorObject getElevator(String name) {
        return elevatorsMap.getOrDefault(name, null);
    }

    public void registerElevator(String name, ElevatorObject elevatorObject) {
        if (this.mainChunk == null) {
            Chunk chunk = elevatorObject.getFloor(1).getBlockUnderMainDoor().getChunk();
            this.mainChunk = new Location(chunk.getWorld(), chunk.getX(), 0, chunk.getZ());
        }
        elevatorObject.setElevatorControllerName(this.controllerName);
        this.elevatorsMap.putIfAbsent(name, elevatorObject);
    }

    public boolean isSingle() {
        return this.elevatorsMap.size() == 1;
    }

    public boolean isEmpty() {
        return this.elevatorsMap.isEmpty();
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
