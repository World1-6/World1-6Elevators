package com.andrew121410.mc.world16elevators;


import com.andrew121410.mc.world16elevators.enums.ElevatorStatus;
import com.andrew121410.mc.world16utils.player.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ElevatorRunnable extends BukkitRunnable {

    private final World16Elevators plugin;
    private final Elevator elevator;

    private final boolean goingUp;
    private final ElevatorFloor elevatorFloor;
    private final ElevatorStatus elevatorStatus;

    private int counter;

    private ElevatorFloor floorThatWeAreGoingToPass;

    public ElevatorRunnable(World16Elevators plugin, Elevator elevator, boolean goingUp, ElevatorFloor elevatorFloor, ElevatorStatus elevatorStatus, int counter, ElevatorFloor floorThatWeAreGoingToPass) {
        this.plugin = plugin;
        this.elevator = elevator;
        this.goingUp = goingUp;
        this.elevatorFloor = elevatorFloor;
        this.elevatorStatus = elevatorStatus;
        this.counter = counter;
        this.floorThatWeAreGoingToPass = floorThatWeAreGoingToPass;
    }

    public ElevatorRunnable(World16Elevators plugin, Elevator elevator, boolean goingUp, ElevatorFloor elevatorFloor, ElevatorStatus elevatorStatus) {
        this(plugin, elevator, goingUp, elevatorFloor, elevatorStatus, (int) elevator.getElevatorSettings().getTicksPerSecond(), null);
    }

    @Override
    public void run() {
        if (elevator.isIdling()) return;
        elevator.reCalculateFloorBuffer(goingUp);
        ElevatorFloor stopByFloor = !elevator.getStopBy().getPriorityQueue().isEmpty() ? elevator.getFloor(elevator.getStopBy().getPriorityQueue().peek()) : null;

        if (floorThatWeAreGoingToPass == null) {
            Integer intFloorThatWeAreGoingToPass = elevator.getFloorBuffer().peek();
            floorThatWeAreGoingToPass = intFloorThatWeAreGoingToPass != null ? elevator.getFloor(intFloorThatWeAreGoingToPass) : null;
        } else {
            // We are passing a floor.
            if (elevator.getElevatorMovement().getAtDoor().getBlockY() == floorThatWeAreGoingToPass.getBlockUnderMainDoor().getBlockY()) {
                if (elevator.getElevatorSettings().getPassingByFloorSound() != null) {
                    elevator.getElevatorMovement().getAtDoor().getWorld().playSound(elevator.getElevatorMovement().getAtDoor(), elevator.getElevatorSettings().getPassingByFloorSound().getSound(), elevator.getElevatorSettings().getPassingByFloorSound().getVolume(), elevator.getElevatorSettings().getPassingByFloorSound().getPitch());
                }
                floorThatWeAreGoingToPass = null;
            }
        }

        // Check's if at the floor if so then stop the elevator.
        if (elevator.getElevatorMovement().getAtDoor().getBlockY() == elevatorFloor.getBlockUnderMainDoor().getBlockY()) {
            elevator.floorStop(elevatorFloor, elevatorStatus);
            return;
        } else if (stopByFloor != null && elevator.getElevatorMovement().getAtDoor().getY() == stopByFloor.getBlockUnderMainDoor().getY()) {
            elevator.floorStop(elevatorFloor, elevatorStatus, elevator.getStopBy(), stopByFloor);
            return;
        }

        // Stop's the elevator if emergencyStop is on.
        if (elevator.isEmergencyStop()) {
            elevator.setIdling(false);
            elevator.setGoing(false);
            elevator.setEmergencyStop(false);
            return;
        }

        // Move the elevator
        elevator.move(1, goingUp);

        // Teleport the passengers
        for (Player player : elevator.getPlayers()) {
            PlayerUtils.smoothTeleport(player, player.getLocation().add(0, goingUp ? 1 : -1, 0));
        }

        // Elevator leveling
        int x = elevator.getElevatorMovement().getAtDoor().getBlockY();
        int z = elevatorFloor.getBlockUnderMainDoor().getBlockY();
        if (elevator.getElevatorSettings().isDoElevatorLeveling()) {
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

        new ElevatorRunnable(plugin, elevator, goingUp, elevatorFloor, elevatorStatus, counter, floorThatWeAreGoingToPass).runTaskLater(plugin, counter);
    }
}
