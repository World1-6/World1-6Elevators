package com.andrew121410.mc.world16elevators;

import com.andrew121410.mc.world16elevators.enums.ElevatorStatus;
import com.andrew121410.mc.world16elevators.enums.ElevatorWho;
import com.andrew121410.mc.world16utils.chat.Translate;
import com.andrew121410.mc.world16utils.worldedit.WorldEdit;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@EqualsAndHashCode
@ToString
@Getter
@Setter
public class Elevator {

    private String elevatorControllerName;

    private String elevatorName;
    private String world;
    private ElevatorMovement elevatorMovement;
    private ElevatorSettings elevatorSettings;

    private BoundingBox boundingBoxExpanded;

    private Map<Integer, ElevatorFloor> floorsMap;

    //TEMP DON'T SAVE
    private World16Elevators plugin;

    private boolean isGoing;
    private boolean isFloorQueueGoing;
    private boolean isIdling;
    private boolean isEmergencyStop;

    private int topFloor = 1;
    private int topBottomFloor = 1;

    //Helpers
    private ElevatorFloorSelectorManager elevatorFloorSelectorManager;
    //...

    private Queue<FloorQueueObject> floorQueueBuffer;
    private Queue<Integer> floorBuffer;
    private StopBy stopBy;

    private boolean isPlayersInItBefore;
    private boolean isPlayersInItAfter;

    private FloorQueueObject whereItsCurrentlyGoing;

    public Elevator(World16Elevators plugin, String nameOfElevator, String world, ElevatorMovement elevatorMovement) {
        this(plugin, nameOfElevator, world, elevatorMovement, new ElevatorSettings(), new HashMap<>());
    }

    public Elevator(World16Elevators plugin, String name, String world, ElevatorMovement elevatorMovement, ElevatorSettings elevatorSettings, Map<Integer, ElevatorFloor> floorsMap) {
        if (plugin != null) this.plugin = plugin;

        this.world = world; //NEEDS TO BE SECOND.

        this.floorsMap = floorsMap;
        this.floorQueueBuffer = new LinkedList<>();
        this.floorBuffer = new LinkedList<>();
        this.stopBy = new StopBy();

        this.elevatorName = name;
        this.elevatorMovement = elevatorMovement;
        this.elevatorSettings = elevatorSettings;

        this.boundingBoxExpanded = this.elevatorMovement.getBoundingBox().clone().expand(1);

        this.isGoing = false;
        this.isIdling = false;
        this.isFloorQueueGoing = false;
        this.isEmergencyStop = false;

        this.isPlayersInItBefore = false;
        this.isPlayersInItAfter = false;

        this.whereItsCurrentlyGoing = null;

        this.elevatorFloorSelectorManager = new ElevatorFloorSelectorManager(plugin, this);

        for (ElevatorFloor value : this.floorsMap.values()) {
            if (value.getFloor() >= 2) {
                this.topFloor++;
            } else if (value.getFloor() < 0) {
                if (topBottomFloor == 1) this.topBottomFloor--;
                this.topBottomFloor--;
            }
        }
    }

    public Collection<Entity> getEntities() {
        return getBukkitWorld().getNearbyEntities(boundingBoxExpanded);
    }

    public Collection<Player> getPlayers() {
        return getEntities().stream().filter(entity -> entity instanceof Player).map(entity -> (Player) entity).collect(Collectors.toList());
    }

    public Collection<UUID> getPlayersUUIDs() {
        return getPlayers().stream().map(Entity::getUniqueId).collect(Collectors.toList());
    }

    public void goToFloor(Player player, String floorName, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        ElevatorFloor elevatorFloor = getFloor(floorName);
        if (elevatorFloor == null) return;
        goToFloor(player, elevatorFloor, elevatorStatus, elevatorWho);
    }

    public void goToFloor(Player player, int floorNumber, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        ElevatorFloor elevatorFloor = getFloor(floorNumber);
        if (elevatorFloor == null) return;
        goToFloor(player, elevatorFloor, elevatorStatus, elevatorWho);
    }

    private void goToFloor(Player player, ElevatorFloor elevatorFloor, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        Collection<UUID> peopleThatAreInTheElevator = getPlayersUUIDs();

        if (elevatorFloor.getPermission() != null && !elevatorFloor.getPermission().isEmpty() && peopleThatAreInTheElevator.contains(player.getUniqueId())) {
            if (player.hasPermission(elevatorFloor.getPermission())) {
                player.sendMessage(Translate.chat("&6Access granted for floor " + elevatorFloor.getName()));
            } else {
                player.sendMessage(Translate.chat("&cYou need &c" + elevatorFloor.getPermission() + " &cto go to this floor!"));
                return;
            }
        }

        if (elevatorStatus == ElevatorStatus.UP && elevatorFloor.getFloor() == this.topFloor)
            elevatorStatus = ElevatorStatus.DOWN;
        if (elevatorStatus == ElevatorStatus.DOWN && elevatorFloor.getFloor() == this.topBottomFloor)
            elevatorStatus = ElevatorStatus.UP;

        // Player is already inside the elevator have a different message for them
        if (peopleThatAreInTheElevator.contains(player.getUniqueId())) {
            player.sendMessage(Translate.chat("&2Going to floor " + elevatorFloor.getName()));
        } else {
            switch (elevatorStatus) {
                case UP ->
                        player.sendMessage(Translate.color("&2[Elevator] &6Called elevator to go to floor " + elevatorFloor.getName() + " to go up"));
                case DOWN ->
                        player.sendMessage(Translate.color("&2[Elevator] &6Called elevator to go to floor " + elevatorFloor.getName() + " to go down"));
                case DONT_KNOW ->
                        player.sendMessage(Translate.color("&2[Elevator] &6Called elevator to go to floor " + elevatorFloor.getName()));
            }
        }

        goToFloor(elevatorFloor.getFloor(), elevatorStatus, elevatorWho);
    }

    public void goToFloor(String floorName, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        ElevatorFloor elevatorFloor = getFloor(floorName);
        if (elevatorFloor == null) return;
        goToFloor(elevatorFloor.getFloor(), elevatorStatus, elevatorWho);
    }

    public void goToFloor(int floorNumber, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        boolean goUp;

        //Gets the floor before the elevator starts ticking.
        ElevatorFloor elevatorFloor = getFloor(floorNumber);

        //Check's if the floor exists; if not then cancel.
        if (elevatorFloor == null) return;

        //Add to the queue if elevator is running or idling.
        if (isGoing || isIdling) {
            if (this.floorBuffer.contains(floorNumber) && elevatorStatus != ElevatorStatus.DONT_KNOW && this.stopBy.toElevatorStatus() == elevatorStatus) {
                this.stopBy.getPriorityQueue().add(floorNumber);
            } else {
                if (isGoing && floorNumber == this.whereItsCurrentlyGoing.getFloorNumber()) return;
                if (isIdling && floorNumber == this.elevatorMovement.getFloor()) return;
                floorQueueBuffer.add(new FloorQueueObject(floorNumber, elevatorStatus));
                setupFloorQueue();
            }
            return;
        }
        isGoing = true;
        isPlayersInItBefore = !getPlayers().isEmpty(); //Checks if player is in it already.
        floorBuffer.clear(); //Clears the floorBuffer

        //Checks if the elevator should go up or down.
        goUp = elevatorFloor.getBlockUnderMainDoor().getY() > this.elevatorMovement.getAtDoor().getY();

        //This calculates what floors it's going to pass going up or down this has to be run before it sets this.elevatorFloor to not a floor.
        calculateFloorBuffer(floorNumber, goUp);

        this.stopBy.setGoUp(goUp);

        this.elevatorMovement.setFloor(null); //Not on a floor.

        this.whereItsCurrentlyGoing = new FloorQueueObject(floorNumber, elevatorStatus);

        // Handle teleport elevator on empty
        if (this.elevatorSettings.isTeleportElevatorOnEmpty() && getPlayers().isEmpty()) {
            Location destination = elevatorFloor.getBlockUnderMainDoor().clone();
            Location elevatorDoor = this.elevatorMovement.getAtDoor().clone();

            int difference;
            if (goUp) {
                destination.subtract(0, 5, 0);
                difference = destination.getBlockY() - elevatorDoor.getBlockY();
            } else {
                destination.add(0, 5, 0);
                difference = elevatorDoor.getBlockY() - destination.getBlockY();
            }
            if (difference >= 6) {
                move(difference, goUp);
            }
        }

        //Start ticking the elevator.
        new ElevatorRunnable(plugin, this, goUp, elevatorFloor, elevatorStatus).runTask(plugin);
    }

    protected void move(int howManyY, boolean goUP) {
        try {
            WorldEdit worldEdit = this.plugin.getOtherPlugins().getWorld16Utils().getClassWrappers().getWorldEdit();
            worldEdit.moveCuboidRegion(getBukkitWorld(), elevatorMovement.getBoundingBox(), new Location(getBukkitWorld(), 0, goUP ? 1 : -1, 0), howManyY);
        } catch (Exception e) {
            Bukkit.broadcast(Translate.miniMessage("<red>Error while trying to move the elevator: " + this.elevatorName + " on controller: " + this.elevatorControllerName));
            e.printStackTrace();
            this.plugin.getPluginLoader().disablePlugin(this.plugin);
            return;
        }

        if (goUP) {
            this.elevatorMovement.getAtDoor().add(0, howManyY, 0);
            this.elevatorMovement.getBoundingBox().shift(0, howManyY, 0);
            this.boundingBoxExpanded.shift(0, howManyY, 0);
        } else {
            this.elevatorMovement.getAtDoor().subtract(0, howManyY, 0);
            this.elevatorMovement.getBoundingBox().shift(0, -howManyY, 0);
            this.boundingBoxExpanded.shift(0, -howManyY, 0);
        }
    }

    // Ran when it actually reaches a floor.
    protected void floorStop(ElevatorFloor elevatorFloor, ElevatorStatus elevatorStatus) {
        this.elevatorMovement.setFloor(elevatorFloor.getFloor());
        this.whereItsCurrentlyGoing = null;
        if (!this.elevatorFloorSelectorManager.isRunning()) elevatorFloorSelectorManager.start();
        //Sound
        if (elevatorSettings.getArrivalSound() != null) {
            elevatorFloor.getBlockUnderMainDoor().getWorld().playSound(elevatorFloor.getBlockUnderMainDoor(), elevatorSettings.getArrivalSound().getSound(), elevatorSettings.getArrivalSound().getVolume(), elevatorSettings.getArrivalSound().getPitch());
        }
        floorDone(elevatorFloor, elevatorStatus);
        doFloorIdle();
        isGoing = false;
    }

    // Ran when it reaches a StopBy floor.
    protected void floorStop(ElevatorFloor elevatorFloor, ElevatorStatus elevatorStatus, StopBy stopBy, ElevatorFloor stopByFloorOp) {
        this.isIdling = true;
        this.isGoing = false;
        stopBy.getPriorityQueue().remove();
        elevatorMovement.setFloor(stopByFloorOp.getFloor());
        floorDone(stopByFloorOp, elevatorStatus);
        doFloorIdle();
        goToFloor(elevatorFloor.getFloor(), elevatorStatus, ElevatorWho.STOP_BY);
    }

    public void emergencyStop() {
        this.isEmergencyStop = true;
    }

    private void floorDone(ElevatorFloor elevatorFloor, ElevatorStatus elevatorStatus) {
        elevatorFloor.doDoor(true, true);
        elevatorFloor.doSigns(this, elevatorStatus, false);

        new BukkitRunnable() {
            @Override
            public void run() {
                elevatorFloor.doSigns(Elevator.this, elevatorStatus, true);
                elevatorFloor.doDoor(false, true);
                isPlayersInItAfter = !getPlayers().isEmpty();
                if (!isPlayersInItAfter) elevatorFloorSelectorManager.stop();
            }
        }.runTaskLater(plugin, elevatorSettings.getDoorHolderTicksPerSecond());
    }

    private void doFloorIdle() {
        isIdling = true;
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> isIdling = false, elevatorSettings.getElevatorWaiterTicksPerSecond());
    }

    private void calculateFloorBuffer(int floor, boolean isUp) {
        // Don't try to calculateFloorBuffer If floor number is null
        // This happens if the elevator was stopped abruptly like using /elevator stop
        if (this.elevatorMovement.getFloor() == null) {
            return;
        }

        if (isUp) for (int num = this.elevatorMovement.getFloor() + 1; num < floor; num++) {
            if (num == 0) continue; //0 won't be used as a floor anymore.
            floorBuffer.add(num);
        }
        else for (int num = this.elevatorMovement.getFloor() - 1; num > floor; num--) {
            if (num == 0) continue; //0 won't be used as a floor anymore.
            floorBuffer.add(num);
        }
    }

    protected void reCalculateFloorBuffer(boolean goUp) {
        Integer peek = this.floorBuffer.peek();
        if (peek == null) return;
        ElevatorFloor elevatorFloor = getFloor(peek);
        if (goUp) {
            if (this.elevatorMovement.getAtDoor().getBlockY() >= elevatorFloor.getBlockUnderMainDoor().getBlockY())
                this.floorBuffer.remove();
        } else {
            if (this.elevatorMovement.getAtDoor().getBlockY() <= elevatorFloor.getBlockUnderMainDoor().getBlockY())
                this.floorBuffer.remove();
        }
    }

    private void setupFloorQueue() {
        //Don't run if it's running already
        if (isFloorQueueGoing) {
            return;
        }
        isFloorQueueGoing = true;

        //Checks every 2 seconds to see if the elevator isn't running or idling if not then go to floor.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isGoing && !isIdling && !floorQueueBuffer.isEmpty()) {
                    FloorQueueObject floorQueueObject = floorQueueBuffer.peek();
                    goToFloor(floorQueueObject.getFloorNumber(), floorQueueObject.getElevatorStatus(), ElevatorWho.FLOOR_QUEUE);
                    floorQueueBuffer.remove();
                } else if (floorQueueBuffer.isEmpty()) {
                    isFloorQueueGoing = false;
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }

    /**
     * Finds floors, and adds them to the elevator
     *
     * @param beginningFloor the floor where to start at
     * @param goingUp        true if going up, false if going down
     */
    public void smartCreateFloors(ElevatorFloor beginningFloor, boolean goingUp) {
        long startTime = Instant.now().toEpochMilli();
        boolean whileLoop = true;

        int beginningFloorNumber = beginningFloor.getFloor();
        Location blockUnderMainDoor = beginningFloor.getBlockUnderMainDoor().clone();
        List<Location> doors = new ArrayList<>();
        doors.add(blockUnderMainDoor);
        doors.addAll(beginningFloor.getDoorList().stream().map(Location::clone).toList());

        ElevatorFloor lastNewFloor = null;
        if (goingUp) {
            beginningFloorNumber++;
            //0 won't be used as a floor anymore.
            if (beginningFloorNumber == 0) beginningFloorNumber++;
            doors.forEach(location -> location.add(0, 1, 0));
            while (whileLoop) {
                for (Location blockLocation : doors) {
                    if (blockLocation.getBlock().getType() != Material.AIR) {
                        Block block1 = blockLocation.getBlock().getRelative(BlockFace.UP);
                        if (block1.getType() == Material.AIR || block1.getType() == Material.IRON_DOOR) {
                            Block block2 = block1.getRelative(BlockFace.UP);
                            if (block2.getType() == Material.AIR || block2.getType() == Material.IRON_DOOR) {
                                Block block3 = block2.getRelative(BlockFace.UP);
                                if (block3.getType() != Material.AIR) {
                                    //We found a floor.

                                    // If it's on the same y level as the floor then it's considered to be an extra door for that floor
                                    if (lastNewFloor != null && lastNewFloor.getBlockUnderMainDoor().getBlockY() == blockLocation.getBlockY()) {
                                        lastNewFloor.getDoorList().add(blockLocation.clone());
                                    } else {
                                        lastNewFloor = new ElevatorFloor(beginningFloorNumber, blockUnderMainDoor.clone());
                                        addFloor(lastNewFloor);
                                        Bukkit.getServer().broadcastMessage("Added floor " + beginningFloorNumber);

                                        beginningFloorNumber++;
                                        //0 won't be used as a floor anymore.
                                        if (beginningFloorNumber == 0) beginningFloorNumber++;
                                    }
                                }
                            }
                        }
                    }
                }
                doors.forEach(location -> location.add(0, 1, 0));
                if (blockUnderMainDoor.getBlockY() >= 319) whileLoop = false;
            }
        } else {
            beginningFloorNumber--;
            //0 won't be used as a floor anymore.
            if (beginningFloorNumber == 0) beginningFloorNumber--;
            doors.forEach(location -> location.subtract(0, 1, 0));
            while (whileLoop) {
                for (Location blockLocation : doors) {
                    if (blockLocation.getBlock().getType() != Material.AIR) {
                        Block block1 = blockLocation.getBlock().getRelative(BlockFace.DOWN);
                        if (block1.getType() == Material.AIR || block1.getType() == Material.IRON_DOOR) {
                            Block block2 = block1.getRelative(BlockFace.DOWN);
                            if (block2.getType() == Material.AIR || block2.getType() == Material.IRON_DOOR) {
                                Block block3 = block2.getRelative(BlockFace.DOWN);
                                if (block3.getType() != Material.AIR) {
                                    //We found a floor.

                                    // If it's on the same y level as the floor then it's considered to be an extra door for that floor
                                    if (lastNewFloor != null && lastNewFloor.getBlockUnderMainDoor().getBlockY() == blockLocation.getBlockY()) {
                                        lastNewFloor.getDoorList().add(blockLocation.clone());
                                    } else {
                                        lastNewFloor = new ElevatorFloor(beginningFloorNumber, blockUnderMainDoor.clone());
                                        addFloor(lastNewFloor);

                                        beginningFloorNumber--;
                                        //0 won't be used as a floor anymore.
                                        if (beginningFloorNumber == 0) beginningFloorNumber--;
                                    }
                                }
                            }
                        }
                    }
                }
                doors.forEach(location -> location.subtract(0, 1, 0));
                if (blockUnderMainDoor.getBlockY() <= -64) whileLoop = false;
            }
        }
        long endTime = Instant.now().toEpochMilli();
        long totalTime = endTime - startTime;
        Bukkit.getServer().broadcastMessage("smartCreateFloors has completed took: " + totalTime + " milliseconds");
    }

    public void addFloor(ElevatorFloor elevatorFloor) {
        //We have to find out the elevator floor by ourself.
        if (elevatorFloor.getFloor() == Integer.MIN_VALUE) {
            //Checks if the elevator should go up or down.
            boolean goUp = elevatorFloor.getBlockUnderMainDoor().getY() > this.elevatorMovement.getAtDoor().getY();
            int a = goUp ? 1 : -1;
            while (this.getFloorsMap().containsKey(a) && a != 0) {
                if (goUp) a++;
                else a--;
            }
            elevatorFloor.setFloor(a);
            Bukkit.getServer().broadcastMessage("New floor has been set to " + a);
        }

        if (this.floorsMap.containsKey(elevatorFloor.getFloor())) return; //Don't add the floor if we already have it.
        if (elevatorFloor.getFloor() == 0) return; //0 won't be used as a floor anymore.

        if (elevatorFloor.getFloor() >= 2) {
            this.topFloor++;
        } else if (elevatorFloor.getFloor() < 0) {
            if (topBottomFloor == 1) this.topBottomFloor--;
            this.topBottomFloor--;
        }
        this.floorsMap.put(elevatorFloor.getFloor(), elevatorFloor);
    }

    public void deleteFloor(int floor) {
        //Don't delete a non exist floor.
        if (!this.floorsMap.containsKey(floor)) return;

        if (floor >= 1) this.topFloor--;
        else if (floor < 0) {
            this.topBottomFloor++;
            if (topBottomFloor == 0) this.topBottomFloor++; //0 won't be used as a floor anymore.
        }
        this.floorsMap.remove(floor);
    }

    public void deleteFloor(String name) {
        ElevatorFloor elevatorFloor = getFloor(name);
        if (elevatorFloor == null) return;
        deleteFloor(elevatorFloor.getFloor());
    }

    public ElevatorFloor getFloor(int floor) {
        if (this.floorsMap.containsKey(floor)) {
            return this.floorsMap.get(floor);
        }
        return null;
    }

    public ElevatorFloor getFloor(String name) {
        ElevatorFloor elevatorFloor = this.floorsMap.values().stream().filter(floorObject1 -> floorObject1.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        if (elevatorFloor == null) {
            Integer integer = null;
            try {
                integer = Integer.parseInt(name);
            } catch (NumberFormatException ignore) {
            }
            if (integer != null) {
                if (this.floorsMap.containsKey(integer)) {
                    elevatorFloor = this.floorsMap.get(integer);
                }
            }
        }
        return elevatorFloor;
    }

    private void arrivalChime(Location location) {
        getBukkitWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 10F, 1.8F);
    }

    private void passingChime(Location location) {
        getBukkitWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 10F, 1.3F);
    }


    public org.bukkit.World getBukkitWorld() {
        return Bukkit.getServer().getWorld(this.world);
    }
}