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
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode
@ToString
@Getter
@Setter
public class ElevatorController {

    private World16Elevators plugin;

    private String controllerName;
    private Location mainChunk;
    private Map<String, Elevator> elevatorsMap;

    public ElevatorController(World16Elevators plugin, String controllerName, Location mainChunk, Map<String, Elevator> elevatorsMap) {
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
        Elevator elevator = getClosestElevator(floorNumber, true, elevatorStatus);
        if (elevator == null) return;
        player.sendMessage(Translate.color("&e&oCalled the nearest elevator on the controller"));
        elevator.goToFloor(player, floorNumber, elevatorStatus, elevatorWho);
    }

    public void callElevatorClosest(Player player, String floorName, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        Elevator elevator = getClosestElevator(floorName, true, elevatorStatus);
        if (elevator == null) return;
        player.sendMessage(Translate.color("&e&oCalled the nearest elevator on the controller"));
        elevator.goToFloor(player, floorName, elevatorStatus, elevatorWho);
    }

    public void callElevatorClosest(int floorNumber, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        Elevator elevator = getClosestElevator(floorNumber, true, elevatorStatus);
        if (elevator == null) return;
        elevator.goToFloor(floorNumber, elevatorStatus, elevatorWho);
    }

    public void callElevatorClosest(String floorName, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        Elevator elevator = getClosestElevator(floorName, true, elevatorStatus);
        if (elevator == null) return;
        elevator.goToFloor(floorName, elevatorStatus, elevatorWho);
    }

    public void callAllElevators(int floorNum, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        this.elevatorsMap.forEach((k, v) -> v.goToFloor(floorNum, elevatorStatus, elevatorWho));
    }

    public Elevator getClosestElevator(int floorNumber, boolean smart, ElevatorStatus elevatorStatus) {
        Optional<Elevator> optionalElevatorObject = this.elevatorsMap.values().stream().filter(elevatorObject -> !elevatorObject.isGoing()).min(Comparator.comparingInt(i -> Math.abs(i.getElevatorMovement().getAtDoor().getBlockY() - i.getFloor(floorNumber).getBlockUnderMainDoor().getBlockY())));
        Optional<Elevator> optionalElevatorObject1 = this.elevatorsMap.values().stream().filter(Elevator::isGoing).filter(elevatorObject -> elevatorObject.getFloorBuffer().contains(floorNumber) && elevatorObject.getStopBy().toElevatorStatus() == elevatorStatus).findFirst();
        return optionalElevatorObject1.orElse(optionalElevatorObject.orElse(null));
    }

    public Elevator getClosestElevator(String floorName, boolean smart, ElevatorStatus elevatorStatus) {
        Optional<Elevator> optionalElevatorObject = this.elevatorsMap.values().stream().filter(elevatorObject -> !elevatorObject.isGoing()).min(Comparator.comparingInt(i -> Math.abs(i.getElevatorMovement().getAtDoor().getBlockY() - i.getFloor(floorName).getBlockUnderMainDoor().getBlockY())));
        Optional<Elevator> optionalElevatorObject1 = this.elevatorsMap.values().stream().filter(Elevator::isGoing).filter(elevatorObject -> elevatorObject.getFloorBuffer().contains(elevatorObject.getFloor(floorName).getFloor()) && elevatorObject.getStopBy().toElevatorStatus() == elevatorStatus).findFirst();
        return optionalElevatorObject1.orElse(optionalElevatorObject.orElse(null));
    }

    public Elevator getElevator(String name) {
        return elevatorsMap.getOrDefault(name, null);
    }

    public void registerElevator(String name, Elevator elevator) {
        if (this.mainChunk == null) {
            Chunk chunk = elevator.getFloor(1).getBlockUnderMainDoor().getChunk();
            this.mainChunk = new Location(chunk.getWorld(), chunk.getX(), 0, chunk.getZ());
        }
        elevator.setElevatorControllerName(this.controllerName);
        this.elevatorsMap.putIfAbsent(name, elevator);
    }

    public boolean isSingle() {
        return this.elevatorsMap.size() == 1;
    }

    public boolean isEmpty() {
        return this.elevatorsMap.isEmpty();
    }
}
