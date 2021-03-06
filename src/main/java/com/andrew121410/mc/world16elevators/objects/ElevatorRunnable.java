package com.andrew121410.mc.world16elevators.objects;


import com.andrew121410.mc.world16elevators.World16Elevators;
import com.andrew121410.mc.world16utils.player.SmoothTeleport;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ElevatorRunnable extends BukkitRunnable {

    private World16Elevators plugin;
    private ElevatorObject elevatorObject;
    private SmoothTeleport smoothTeleport;

    private boolean goUP;
    private FloorObject floorObject;
    private ElevatorStatus elevatorStatus;

    private int counter;

    private FloorObject floorThatWeAreGoingToPass;

    public ElevatorRunnable(World16Elevators plugin, ElevatorObject elevatorObject, boolean goUP, FloorObject floorObject, ElevatorStatus elevatorStatus, int counter, FloorObject floorThatWeAreGoingToPass) {
        this.plugin = plugin;
        this.elevatorObject = elevatorObject;
        this.smoothTeleport = this.plugin.getOtherPlugins().getWorld16Utils().getClassWrappers().getSmoothTeleport();
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
            //We are passing a floor.
            if (elevatorObject.getElevatorMovement().getAtDoor().getBlockY() == floorThatWeAreGoingToPass.getMainDoor().getBlockY()) {
                if (elevatorObject.getElevatorSettings().getPassingByFloorSound() != null) {
                    elevatorObject.getElevatorMovement().getAtDoor().getWorld().playSound(elevatorObject.getElevatorMovement().getAtDoor(), elevatorObject.getElevatorSettings().getPassingByFloorSound().getSound(), elevatorObject.getElevatorSettings().getPassingByFloorSound().getVolume(), elevatorObject.getElevatorSettings().getPassingByFloorSound().getPitch());
                }
                floorThatWeAreGoingToPass = null;
            }
        }

//        Check's if at the floor if so then stop the elevator.
        if (elevatorObject.getElevatorMovement().getAtDoor().getBlockY() == floorObject.getMainDoor().getBlockY()) {
            this.cancel();
            elevatorObject.floorStop(floorObject, elevatorStatus);
            return;
        } else if (stopByFloor != null && elevatorObject.getElevatorMovement().getAtDoor().getY() == stopByFloor.getMainDoor().getY()) {
            elevatorObject.floorStop(floorObject, elevatorStatus, elevatorObject.getStopBy(), stopByFloor);
            return;
        }

//       Stop's the elevator if emergencyStop is on.
        if (elevatorObject.isEmergencyStop()) {
            elevatorObject.setIdling(false);
            elevatorObject.setGoing(false);
            elevatorObject.setEmergencyStop(false);
            this.cancel();
            return;
        }

        if (goUP) {
            elevatorObject.goUP();

            //TP THEM UP 1
            for (Player player : elevatorObject.getPlayers()) {
                smoothTeleport.teleport(player, player.getLocation().add(0, 1, 0));
            }

            if (elevatorObject.getElevatorSettings().isDoElevatorLeveling()) {
                int x = elevatorObject.getElevatorMovement().getAtDoor().getBlockY();
                int z = floorObject.getMainDoor().getBlockY();
                x += 5;
                if (x >= z) {
                    counter += 1;
                }
            }
        } else {
            elevatorObject.goDOWN();

            //TP THEM DOWN 1
            for (Player player : elevatorObject.getPlayers()) {
                smoothTeleport.teleport(player, player.getLocation().subtract(0, 1, 0));
            }

            if (elevatorObject.getElevatorSettings().isDoElevatorLeveling()) {
                int x = elevatorObject.getElevatorMovement().getAtDoor().getBlockY();
                int z = floorObject.getMainDoor().getBlockY();
                x -= 5;
                if (x <= z) {
                    counter += 1;
                }
            }
        }
        this.cancel();
        new ElevatorRunnable(plugin, elevatorObject, goUP, floorObject, elevatorStatus, counter, floorThatWeAreGoingToPass).runTaskLater(plugin, counter);
    }
}
