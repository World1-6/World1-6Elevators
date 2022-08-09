package com.andrew121410.mc.world16elevators;


import com.andrew121410.mc.world16elevators.enums.ElevatorStatus;
import com.andrew121410.mc.world16utils.player.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ElevatorRunnable extends BukkitRunnable {

    private final World16Elevators plugin;
    private final ElevatorObject elevatorObject;

    private final boolean goingUp;
    private final FloorObject floorObject;
    private final ElevatorStatus elevatorStatus;

    private int counter;

    private FloorObject floorThatWeAreGoingToPass;

    public ElevatorRunnable(World16Elevators plugin, ElevatorObject elevatorObject, boolean goingUp, FloorObject floorObject, ElevatorStatus elevatorStatus, int counter, FloorObject floorThatWeAreGoingToPass) {
        this.plugin = plugin;
        this.elevatorObject = elevatorObject;
        this.goingUp = goingUp;
        this.floorObject = floorObject;
        this.elevatorStatus = elevatorStatus;
        this.counter = counter;
        this.floorThatWeAreGoingToPass = floorThatWeAreGoingToPass;
    }

    public ElevatorRunnable(World16Elevators plugin, ElevatorObject elevatorObject, boolean goingUp, FloorObject floorObject, ElevatorStatus elevatorStatus) {
        this(plugin, elevatorObject, goingUp, floorObject, elevatorStatus, (int) elevatorObject.getElevatorSettings().getTicksPerSecond(), null);
    }

    @Override
    public void run() {
        if (elevatorObject.isIdling()) return;
        elevatorObject.reCalculateFloorBuffer(goingUp);
        FloorObject stopByFloor = !elevatorObject.getStopBy().getPriorityQueue().isEmpty() ? elevatorObject.getFloor(elevatorObject.getStopBy().getPriorityQueue().peek()) : null;

        if (floorThatWeAreGoingToPass == null) {
            Integer intFloorThatWeAreGoingToPass = elevatorObject.getFloorBuffer().peek();
            floorThatWeAreGoingToPass = intFloorThatWeAreGoingToPass != null ? elevatorObject.getFloor(intFloorThatWeAreGoingToPass) : null;
        } else {
            // We are passing a floor.
            if (elevatorObject.getElevatorMovement().getAtDoor().getBlockY() == floorThatWeAreGoingToPass.getBlockUnderMainDoor().getBlockY()) {
                if (elevatorObject.getElevatorSettings().getPassingByFloorSound() != null) {
                    elevatorObject.getElevatorMovement().getAtDoor().getWorld().playSound(elevatorObject.getElevatorMovement().getAtDoor(), elevatorObject.getElevatorSettings().getPassingByFloorSound().getSound(), elevatorObject.getElevatorSettings().getPassingByFloorSound().getVolume(), elevatorObject.getElevatorSettings().getPassingByFloorSound().getPitch());
                }
                floorThatWeAreGoingToPass = null;
            }
        }

        // Check's if at the floor if so then stop the elevator.
        if (elevatorObject.getElevatorMovement().getAtDoor().getBlockY() == floorObject.getBlockUnderMainDoor().getBlockY()) {
            elevatorObject.floorStop(floorObject, elevatorStatus);
            return;
        } else if (stopByFloor != null && elevatorObject.getElevatorMovement().getAtDoor().getY() == stopByFloor.getBlockUnderMainDoor().getY()) {
            elevatorObject.floorStop(floorObject, elevatorStatus, elevatorObject.getStopBy(), stopByFloor);
            return;
        }

        // Stop's the elevator if emergencyStop is on.
        if (elevatorObject.isEmergencyStop()) {
            elevatorObject.setIdling(false);
            elevatorObject.setGoing(false);
            elevatorObject.setEmergencyStop(false);
            return;
        }

        // Move the elevator
        elevatorObject.move(1, goingUp);

        // Teleport the passengers
        for (Player player : elevatorObject.getPlayers()) {
            PlayerUtils.smoothTeleport(player, player.getLocation().add(0, goingUp ? 1 : -1, 0));
        }

        // Elevator leveling
        int x = elevatorObject.getElevatorMovement().getAtDoor().getBlockY();
        int z = floorObject.getBlockUnderMainDoor().getBlockY();
        if (elevatorObject.getElevatorSettings().isDoElevatorLeveling()) {
            if (goingUp) {
                x += 5;
                if (x >= z) counter += 1;
            } else {
                x -= 5;
                if (x <= z) counter += 1;
            }
        }

        // Don't try to register another task if the plugin is disabled.
        if (!this.plugin.isEnabled()) return;

        new ElevatorRunnable(plugin, elevatorObject, goingUp, floorObject, elevatorStatus, counter, floorThatWeAreGoingToPass).runTaskLater(plugin, counter);
    }
}
