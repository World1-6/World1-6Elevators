package com.andrew121410.mc.world16elevators;


import com.andrew121410.mc.world16elevators.enums.ElevatorStatus;
import com.andrew121410.mc.world16utils.player.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ElevatorRunnable extends BukkitRunnable {

    private World16Elevators plugin;
    private ElevatorObject elevatorObject;

    private boolean goUP;
    private FloorObject floorObject;
    private ElevatorStatus elevatorStatus;

    private int counter;

    private FloorObject floorThatWeAreGoingToPass;

    public ElevatorRunnable(World16Elevators plugin, ElevatorObject elevatorObject, boolean goUP, FloorObject floorObject, ElevatorStatus elevatorStatus, int counter, FloorObject floorThatWeAreGoingToPass) {
        this.plugin = plugin;
        this.elevatorObject = elevatorObject;
        this.goUP = goUP;
        this.floorObject = floorObject;
        this.elevatorStatus = elevatorStatus;
        this.counter = counter;
        this.floorThatWeAreGoingToPass = floorThatWeAreGoingToPass;
    }

    public ElevatorRunnable(World16Elevators plugin, ElevatorObject elevatorObject, boolean goUp, FloorObject floorObject, ElevatorStatus elevatorStatus) {
        this(plugin, elevatorObject, goUp, floorObject, elevatorStatus, (int) elevatorObject.getElevatorSettings().getTicksPerSecond(), null);
    }

    @Override
    public void run() {
        if (elevatorObject.isIdling()) return;
        elevatorObject.reCalculateFloorBuffer(goUP);
        FloorObject stopByFloor = !elevatorObject.getStopBy().getStopByQueue().isEmpty() ? elevatorObject.getFloor(elevatorObject.getStopBy().getStopByQueue().peek()) : null;

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
            this.cancel();
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
            this.cancel();
            return;
        }

        elevatorObject.move(1, goUP);
        if (goUP) {
            // Teleport them up 1
            for (Player player : elevatorObject.getPlayers()) {
                PlayerUtils.smoothTeleport(player, player.getLocation().add(0, 1, 0));
            }

            if (elevatorObject.getElevatorSettings().isDoElevatorLeveling()) {
                int x = elevatorObject.getElevatorMovement().getAtDoor().getBlockY();
                int z = floorObject.getBlockUnderMainDoor().getBlockY();
                x += 5;
                if (x >= z) {
                    counter += 1;
                }
            }
        } else {
            // Teleport them down 1
            for (Player player : elevatorObject.getPlayers()) {
                PlayerUtils.smoothTeleport(player, player.getLocation().subtract(0, 1, 0));
            }

            if (elevatorObject.getElevatorSettings().isDoElevatorLeveling()) {
                int x = elevatorObject.getElevatorMovement().getAtDoor().getBlockY();
                int z = floorObject.getBlockUnderMainDoor().getBlockY();
                x -= 5;
                if (x <= z) {
                    counter += 1;
                }
            }
        }
        this.cancel();

        // Don't try to register another task if the plugin is disabled.
        if (!this.plugin.isEnabled()) return;

        new ElevatorRunnable(plugin, elevatorObject, goUP, floorObject, elevatorStatus, counter, floorThatWeAreGoingToPass).runTaskLater(plugin, counter);
    }
}
