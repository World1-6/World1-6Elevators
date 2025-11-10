package com.andrew121410.mc.world16elevators;


import com.andrew121410.mc.world16elevators.enums.ElevatorStatus;
import com.andrew121410.mc.world16utils.chat.Translate;
import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ElevatorRunnable extends BukkitRunnable {

    private final World16Elevators plugin;
    private final Elevator elevator;
    private final boolean goingUp;
    private final ElevatorFloor targetFloor;
    private final ElevatorStatus elevatorStatus;

    private ElevatorFloor floorThatWeAreGoingToPass;
    private final int maxFloorY;
    private final int minFloorY;

    private int ticksPerMove;
    private int tickCounter;

    public ElevatorRunnable(World16Elevators plugin, Elevator elevator, boolean goingUp, ElevatorFloor targetFloor, ElevatorStatus elevatorStatus, int minFloorY, int maxFloorY) {
        this.plugin = plugin;
        this.elevator = elevator;
        this.goingUp = goingUp;
        this.targetFloor = targetFloor;
        this.elevatorStatus = elevatorStatus;
        this.minFloorY = minFloorY;
        this.maxFloorY = maxFloorY;

        this.ticksPerMove = (int) elevator.getElevatorSettings().getTicksPerSecond();
        this.tickCounter = 0;
    }

    public void startElevator() {
        this.runTaskTimer(plugin, 20L, 1L);
    }

    @Override
    public void run() {
        if (elevator.isIdling()) {
            this.cancel();
            return;
        }

        // Increment the tick counter
        tickCounter++;

        // Increment the tick counter and move the elevator only if the counter reaches the ticksPerMove threshold
        // For example, if ticksPerMove is 6, the elevator will move every 6 ticks
        if (tickCounter < ticksPerMove) {
            return; // If it's not time yet, return early and wait for the next tick
        }

        // Reset the tick counter after a move
        tickCounter = 0;

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
        if (elevator.getElevatorMovement().getAtDoor().getBlockY() == targetFloor.getBlockUnderMainDoor().getBlockY()) {
            elevator.floorStop(targetFloor, elevatorStatus);
            this.cancel();
            return;
        } else if (stopByFloor != null && elevator.getElevatorMovement().getAtDoor().getY() == stopByFloor.getBlockUnderMainDoor().getY()) {
            elevator.floorStop(targetFloor, elevatorStatus, elevator.getStopBy(), stopByFloor);
            this.cancel();
            return;
        }

        // Safety Check
        if (elevator.getElevatorMovement().getAtDoor().getY() > maxFloorY || elevator.getElevatorMovement().getAtDoor().getY() < minFloorY) {
            this.elevator.setEmergencyStop(true);
            Bukkit.broadcast(Translate.miniMessage("<red>Emergency Stop has been activated due to elevator going past its designated floor range"), "world16elevators.admin");
            Bukkit.broadcast(Translate.miniMessage("<red>Elevator: <white>" + elevator.getElevatorName() + " <red>on the Controller: <white>" + elevator.getElevatorControllerName()), "world16elevators.admin");
        }

        // Stop's the elevator if emergencyStop is on.
        if (elevator.isEmergencyStop()) {
            elevator.setIdling(false);
            elevator.setGoing(false);
            elevator.setEmergencyStop(false);
            this.cancel();
            return;
        }

        // Move the elevator
        elevator.move(1, goingUp);

        // Teleport the passengers
        for (Player player : elevator.getPlayers()) {
            player.teleport(
                    player.getLocation().add(0.00, goingUp ? 1 : -1, 0.00),
                    PlayerTeleportEvent.TeleportCause.PLUGIN,
                    TeleportFlag.Relative.VELOCITY_ROTATION
            );
        }

        // Elevator leveling (slows down the elevator as it approaches the target floor)
        int currentY = elevator.getElevatorMovement().getAtDoor().getBlockY();
        int targetY = targetFloor.getBlockUnderMainDoor().getBlockY();
        if (elevator.getElevatorSettings().isDoElevatorLeveling()) {
            if (goingUp) {
                currentY += 5;
                if (currentY >= targetY) {
                    ticksPerMove += 1;
                }
            } else {
                currentY -= 5;
                if (currentY <= targetY) {
                    ticksPerMove += 1;
                }
            }
        }
    }
}
