package com.andrew121410.mc.world16elevators.objects;

import com.andrew121410.mc.world16elevators.World16Elevators;
import com.andrew121410.mc.world16utils.chat.Translate;
import com.andrew121410.mc.world16utils.worldedit.WorldEdit;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
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
@SerializableAs("ElevatorObject")
public class ElevatorObject implements ConfigurationSerializable {

    private String elevatorControllerName;

    private String elevatorName;
    private String world;
    private ElevatorMovement elevatorMovement;
    private ElevatorSettings elevatorSettings;

    private BoundingBox boundingBoxExpanded;

    private Map<Integer, FloorObject> floorsMap;

    //TEMP DON'T SAVE
    private World16Elevators plugin;

    private boolean isGoing;
    private boolean isFloorQueueGoing;
    private boolean isIdling;
    private boolean isEmergencyStop;

    private int topFloor = 1;
    private int topBottomFloor = 1;

    //Helpers
    private ElevatorMessageHelper elevatorMessageHelper;
    //...

    private Queue<FloorQueueObject> floorQueueBuffer;
    private Queue<Integer> floorBuffer;
    private StopBy stopBy;

    private boolean isPlayersInItBefore;
    private boolean isPlayersInItAfter;

    private FloorQueueObject whereItsCurrentlyGoing;

    public ElevatorObject(World16Elevators plugin, String nameOfElevator, String world, ElevatorMovement elevatorMovement) {
        this(plugin, nameOfElevator, world, elevatorMovement, new ElevatorSettings(), new HashMap<>());
    }

    public ElevatorObject(World16Elevators plugin, String name, String world, ElevatorMovement elevatorMovement, ElevatorSettings elevatorSettings, Map<Integer, FloorObject> floorsMap) {
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

        //Helpers
        this.elevatorMessageHelper = new ElevatorMessageHelper(plugin, this);

        for (FloorObject value : this.floorsMap.values()) {
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

    public void callElevator(int whatFloor, int toWhatFloor, ElevatorWho elevatorWho) {
        goToFloor(whatFloor, ElevatorStatus.DONT_KNOW, elevatorWho);
        goToFloor(toWhatFloor, ElevatorStatus.DONT_KNOW, elevatorWho);
    }

    public void goToFloor(Player player, String floorName, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        FloorObject floorObject = getFloor(floorName);
        if (floorObject == null) return;
        goToFloor(player, floorObject, elevatorStatus, elevatorWho);
    }

    public void goToFloor(Player player, int floorNumber, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        FloorObject floorObject = getFloor(floorNumber);
        if (floorObject == null) return;
        goToFloor(player, floorObject, elevatorStatus, elevatorWho);
    }

    private void goToFloor(Player player, FloorObject floorObject, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        if (elevatorStatus == ElevatorStatus.UP && floorObject.getFloor() == this.topFloor)
            elevatorStatus = ElevatorStatus.DOWN;
        if (elevatorStatus == ElevatorStatus.DOWN && floorObject.getFloor() == this.topBottomFloor)
            elevatorStatus = ElevatorStatus.UP;

        switch (elevatorStatus) {
            case UP ->
                    player.sendMessage(Translate.color("&2[Elevator] &6Called elevator to go to floor " + floorObject.getName() + " to go up"));
            case DOWN ->
                    player.sendMessage(Translate.color("&2[Elevator] &6Called elevator to go to floor " + floorObject.getName() + " to go down"));
            case DONT_KNOW ->
                    player.sendMessage(Translate.color("&2[Elevator] &6Called elevator to go to floor " + floorObject.getName()));
        }

        goToFloor(floorObject.getFloor(), elevatorStatus, elevatorWho);
    }

    public void goToFloor(String floorName, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        FloorObject floorObject = getFloor(floorName);
        if (floorObject == null) return;
        goToFloor(floorObject.getFloor(), elevatorStatus, elevatorWho);
    }

    public void goToFloor(int floorNumber, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        boolean goUp;

        //Gets the floor before the elevator starts ticking.
        FloorObject floorObject = getFloor(floorNumber);

        //Check's if the floor exists; if not then cancel.
        if (floorObject == null) return;

        //Add to the queue if elevator is running or idling.
        if (isGoing || isIdling) {
            if (this.floorBuffer.contains(floorNumber) && elevatorStatus != ElevatorStatus.DONT_KNOW && this.stopBy.toElevatorStatus() == elevatorStatus) {
                this.stopBy.getStopByQueue().add(floorNumber);
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
        goUp = floorObject.getBlockUnderMainDoor().getY() > this.elevatorMovement.getAtDoor().getY();

        //This calculates what floors it's going to pass going up or down this has to be run before it sets this.elevatorFloor to not a floor.
        calculateFloorBuffer(floorNumber, goUp);

        this.stopBy.setGoUp(goUp);

        this.elevatorMovement.setFloor(null); //Not on a floor.

        this.whereItsCurrentlyGoing = new FloorQueueObject(floorNumber, elevatorStatus);

        //Start ticking the elevator.
        new ElevatorRunnable(plugin, this, goUp, floorObject, elevatorStatus).runTask(plugin);
    }

    //Ran when it actually reaches a floor.
    protected void floorStop(FloorObject floorObject, ElevatorStatus elevatorStatus) {
        this.elevatorMovement.setFloor(floorObject.getFloor());
        this.whereItsCurrentlyGoing = null;
        if (!this.elevatorMessageHelper.isRunning()) elevatorMessageHelper.start();
        //Sound
        if (elevatorSettings.getArrivalSound() != null) {
            floorObject.getBlockUnderMainDoor().getWorld().playSound(floorObject.getBlockUnderMainDoor(), elevatorSettings.getArrivalSound().getSound(), elevatorSettings.getArrivalSound().getVolume(), elevatorSettings.getArrivalSound().getPitch());
        }
        floorDone(floorObject, elevatorStatus);
        doFloorIdle();
        isGoing = false;
    }

    //Ran when it reaches a StopBy floor.
    protected void floorStop(FloorObject floorObject, ElevatorStatus elevatorStatus, StopBy stopBy, FloorObject stopByFloorOp) {
        isIdling = true;
        stopBy.getStopByQueue().remove();
        elevatorMovement.setFloor(stopByFloorOp.getFloor());
        floorDone(stopByFloorOp, elevatorStatus);
        doFloorIdle();
    }

    protected void goUP() {
        WorldEdit worldEdit = this.plugin.getOtherPlugins().getWorld16Utils().getClassWrappers().getWorldEdit();
        worldEdit.moveCuboidRegion(getBukkitWorld(), elevatorMovement.getBoundingBox(), new Location(getBukkitWorld(), 0, 1, 0), 1);

        elevatorMovement.moveUP();
        this.boundingBoxExpanded.shift(0, 1, 0);
    }

    protected void goDOWN() {
        WorldEdit worldEdit = this.plugin.getOtherPlugins().getWorld16Utils().getClassWrappers().getWorldEdit();
        worldEdit.moveCuboidRegion(getBukkitWorld(), elevatorMovement.getBoundingBox(), new Location(getBukkitWorld(), 0, -1, 0), 1);

        elevatorMovement.moveDOWN();
        this.boundingBoxExpanded.shift(0, -1, 0);
    }

    public void emergencyStop() {
        this.isEmergencyStop = true;
    }

    private void floorDone(FloorObject floorObject, ElevatorStatus elevatorStatus) {
        floorObject.doDoor(true, true);
        floorObject.doSigns(this, elevatorStatus, false);

        new BukkitRunnable() {
            @Override
            public void run() {
                floorObject.doSigns(ElevatorObject.this, elevatorStatus, true);
                floorObject.doDoor(false, true);
                isPlayersInItAfter = !getPlayers().isEmpty();
                if (!isPlayersInItAfter) elevatorMessageHelper.stop();
            }
        }.runTaskLater(plugin, elevatorSettings.getDoorHolderTicksPerSecond());
    }

    private void doFloorIdle() {
        isIdling = true;
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> isIdling = false, elevatorSettings.getElevatorWaiterTicksPerSecond());
    }

    private void calculateFloorBuffer(int floor, boolean isUp) {
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
        FloorObject floorObject = getFloor(peek);
        if (goUp) {
            if (this.elevatorMovement.getAtDoor().getBlockY() >= floorObject.getBlockUnderMainDoor().getBlockY())
                this.floorBuffer.remove();
        } else {
            if (this.elevatorMovement.getAtDoor().getBlockY() <= floorObject.getBlockUnderMainDoor().getBlockY())
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

    public void smartCreateFloors(FloorObject beginningFloor, boolean goUP) {
        long startTime = Instant.now().toEpochMilli();
        boolean whileLoop = true;

        int beginningFloorNumber = beginningFloor.getFloor();
        Location blockUnderMainDoor = beginningFloor.getBlockUnderMainDoor().clone();
        List<Location> doors = new ArrayList<>();
        doors.add(blockUnderMainDoor);
        doors.addAll(beginningFloor.getDoorList().stream().map(Location::clone).toList());

        if (goUP) {
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

                                    FloorObject floorObject = getFloor(beginningFloorNumber);
                                    //If there is already a floor, then this means we just add the door to the doorList for that floor
                                    if (floorObject != null) {
                                        floorObject.getDoorList().add(blockLocation.clone());
                                    } else {
                                        beginningFloorNumber++;

                                        //0 won't be used as a floor anymore.
                                        if (beginningFloorNumber == 0) {
                                            beginningFloorNumber++;
                                        }

                                        floorObject = new FloorObject(beginningFloorNumber, blockUnderMainDoor.clone());
                                        addFloor(floorObject);
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

                                    FloorObject floorObject = getFloor(beginningFloorNumber);
                                    //If there is already a floor, then this means we just add the door to the doorList for that floor
                                    if (floorObject != null) {
                                        floorObject.getDoorList().add(blockLocation.clone());
                                    } else {
                                        beginningFloorNumber--;

                                        //0 won't be used as a floor anymore.
                                        if (beginningFloorNumber == 0) {
                                            beginningFloorNumber--;
                                        }

                                        floorObject = new FloorObject(beginningFloorNumber, blockUnderMainDoor.clone());
                                        addFloor(floorObject);
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

    public void addFloor(FloorObject floorObject) {
        //We have to find out the elevator floor by ourself.
        if (floorObject.getFloor() == Integer.MIN_VALUE) {
            //Checks if the elevator should go up or down.
            boolean goUp = floorObject.getBlockUnderMainDoor().getY() > this.elevatorMovement.getAtDoor().getY();
            int a = goUp ? 1 : -1;
            while (this.getFloorsMap().containsKey(a) && a != 0) {
                if (goUp) a++;
                else a--;
            }
            floorObject.setFloor(a);
            Bukkit.getServer().broadcastMessage("New floor has been set to " + a);
        }

        if (this.floorsMap.containsKey(floorObject.getFloor())) return; //Don't add the floor if we already have it.
        if (floorObject.getFloor() == 0) return; //0 won't be used as a floor anymore.

        if (floorObject.getFloor() >= 2) {
            this.topFloor++;
        } else if (floorObject.getFloor() < 0) {
            if (topBottomFloor == 1) this.topBottomFloor--;
            this.topBottomFloor--;
        }
        this.floorsMap.put(floorObject.getFloor(), floorObject);
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
        FloorObject floorObject = getFloor(name);
        if (floorObject == null) return;
        deleteFloor(floorObject.getFloor());
    }

    public FloorObject getFloor(int floor) {
        if (this.floorsMap.containsKey(floor)) {
            return this.floorsMap.get(floor);
        }
        return null;
    }

    public FloorObject getFloor(String name) {
        FloorObject floorObject = this.floorsMap.values().stream().filter(floorObject1 -> floorObject1.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        if (floorObject == null) {
            Integer integer = null;
            try {
                integer = Integer.parseInt(name);
            } catch (NumberFormatException ignore) {
            }
            if (integer != null) {
                if (this.floorsMap.containsKey(integer)) {
                    floorObject = this.floorsMap.get(integer);
                }
            }
        }
        return floorObject;
    }

    public void clickMessageGoto(Player player) {
        ComponentBuilder componentBuilder = new ComponentBuilder().append("[").color(ChatColor.WHITE).append("BexarSystems").color(ChatColor.GOLD).append(" - ").color(ChatColor.RED).append("Please click a floor in the chat to take the elevator to.").color(ChatColor.BLUE).append("]").color(ChatColor.WHITE).append("\n");
        for (Map.Entry<Integer, FloorObject> floorObjectEntry : this.floorsMap.entrySet()) {
            Integer integer = floorObjectEntry.getKey();
            FloorObject floorObject = floorObjectEntry.getValue();

            componentBuilder.reset().append(new ComponentBuilder(floorObject.getName() + ",")
                    .color(ChatColor.GOLD)
                    .bold(true)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elevator call " + elevatorControllerName + " " + elevatorName + " " + floorObject.getName()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click me to go to: " + floorObject.getName())))
                    .append(" ")
                    .create());
        }
        player.spigot().sendMessage(componentBuilder.create());
    }

    public void elevatorFloorsMessage(Player player) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&2[Elevator Floors]&r");
        for (Map.Entry<Integer, FloorObject> entry : this.floorsMap.entrySet()) {
            Integer floor = entry.getKey();
            FloorObject floorObject = entry.getValue();
            stringBuilder.append("&e, &a" + floorObject.getName());
        }
        player.sendMessage(Translate.color(stringBuilder.toString()));
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

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("Name", this.elevatorName);
        map.put("World", this.world);
        map.put("Shaft", this.elevatorMovement);
        map.put("Settings", this.elevatorSettings);
        map.put("FloorMap", this.floorsMap);
        return map;
    }

    public static ElevatorObject deserialize(Map<String, Object> map) {
        return new ElevatorObject(World16Elevators.getInstance(), (String) map.get("Name"), (String) map.get("World"), (ElevatorMovement) map.get("Shaft"), (ElevatorSettings) map.get("Settings"), (Map<Integer, FloorObject>) map.get("FloorMap"));
    }
}