package com.andrew121410.mc.world16elevators.objects;

import com.andrew121410.mc.world16elevators.World16Elevators;
import com.andrew121410.mc.world16utils.math.SimpleMath;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

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

    //Bounding BOX
    private Location locationDownPLUS;
    private Location locationUpPLUS;
    //...

    private Map<Integer, FloorObject> floorsMap;

    //TEMP DON'T SAVE
    private World16Elevators plugin;

    private boolean isGoing;
    private boolean isFloorQueueGoing;
    private boolean isIdling;
    private boolean isEmergencyStop;

    private int topFloor = 0;
    private int topBottomFloor = 0;

    //Helpers
    private ElevatorMessageHelper elevatorMessageHelper;
    //...

    private Queue<FloorQueueObject> floorQueueBuffer;
    private Queue<Integer> floorBuffer;
    private StopBy stopBy;

    private boolean isPlayersInItBefore;
    private boolean isPlayersInItAfter;

    private FloorQueueObject whereItsCurrentlyGoing;

    public ElevatorObject(World16Elevators plugin, String nameOfElevator, String world, ElevatorMovement elevatorMovement, BoundingBox boundingBox) {
        this(plugin, nameOfElevator, world, elevatorMovement, new ElevatorSettings(), boundingBox, new HashMap<>());
    }

    public ElevatorObject(World16Elevators plugin, String name, String world, ElevatorMovement elevatorMovement, ElevatorSettings elevatorSettings, BoundingBox boundingBox, Map<Integer, FloorObject> floorsMap) {
        if (plugin != null) this.plugin = plugin;

        this.world = world; //NEEDS TO BE SECOND.

        this.floorsMap = floorsMap;
        this.floorQueueBuffer = new LinkedList<>();
        this.floorBuffer = new LinkedList<>();
        this.stopBy = new StopBy();

        this.elevatorName = name;
        this.elevatorMovement = elevatorMovement;
        this.elevatorSettings = elevatorSettings;

        this.locationDownPLUS = boundingBox.getMin().toLocation(getBukkitWorld());
        this.locationUpPLUS = boundingBox.getMax().toLocation(getBukkitWorld());

        this.isGoing = false;
        this.isIdling = false;
        this.isFloorQueueGoing = false;
        this.isEmergencyStop = false;

        this.isPlayersInItBefore = false;
        this.isPlayersInItAfter = false;

        this.whereItsCurrentlyGoing = null;

        //Helpers
        this.elevatorMessageHelper = new ElevatorMessageHelper(plugin, this);
    }

    public Collection<Entity> getEntities() {
        return getBukkitWorld().getNearbyEntities(SimpleMath.toBoundingBox(locationDownPLUS.toVector(), locationUpPLUS.toVector()));
    }

    public Collection<Player> getPlayers() {
        return getBukkitWorld().getNearbyEntities(SimpleMath.toBoundingBox(locationDownPLUS.toVector(), locationUpPLUS.toVector())).stream().filter(entity -> entity instanceof Player).map(entity -> (Player) entity).collect(Collectors.toList());
    }

    public void callElevator(int whatFloor, int toWhatFloor, ElevatorWho elevatorWho) {
        goToFloor(whatFloor, ElevatorStatus.DONT_KNOW, elevatorWho);
        goToFloor(toWhatFloor, ElevatorStatus.DONT_KNOW, elevatorWho);
    }

    public void goToFloor(String name, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        Integer isIntInt = null;
        try {
            Integer.parseInt(name);
            isIntInt = Integer.valueOf(name);
        } catch (Exception ignored) {
        }
        if (isIntInt != null) {
            goToFloor(isIntInt, elevatorStatus, elevatorWho);
            return;
        }
        FloorObject floorObject = getFloor(name);
        if (floorObject == null) return;
        goToFloor(floorObject.getFloor(), elevatorStatus, elevatorWho);
    }

    public void goToFloor(int floorNum, ElevatorStatus elevatorStatus, ElevatorWho elevatorWho) {
        boolean goUp;

        //Gets the floor before the elevator starts ticking.
        FloorObject floorObject = getFloor(floorNum);

        //Check if the floor is a thing or not.
        if (floorObject == null) return;

        //Add to the queue if elevator is running or idling.
        if (isGoing || isIdling) {
            if (this.floorBuffer.contains(floorNum) && elevatorStatus != ElevatorStatus.DONT_KNOW && this.stopBy.toElevatorStatus() == elevatorStatus) {
                this.stopBy.getStopByQueue().add(floorNum);
            } else {
                floorQueueBuffer.add(new FloorQueueObject(floorNum, elevatorStatus));
                setupFloorQueue();
            }
            return;
        }
        isGoing = true;
        isPlayersInItBefore = !getPlayers().isEmpty(); //Checks if player is in it already.
        floorBuffer.clear(); //Clears the floorBuffer

        //Checks if the elevator should go up or down.
        goUp = floorObject.getMainDoor().getY() > this.elevatorMovement.getAtDoor().getY();

        //This calculates what floors it's going to pass going up or down this has to be run before it sets this.elevatorFloor to not a floor.
        calculateFloorBuffer(floorNum, goUp);

        this.stopBy.setGoUp(goUp);

        this.elevatorMovement.setFloor(null); //Not on a floor.

        this.whereItsCurrentlyGoing = new FloorQueueObject(floorNum, elevatorStatus);

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
            floorObject.getMainDoor().getWorld().playSound(floorObject.getMainDoor(), elevatorSettings.getArrivalSound().getSound(), elevatorSettings.getArrivalSound().getVolume(), elevatorSettings.getArrivalSound().getPitch());
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
        WorldEditPlugin worldEditPlugin = this.plugin.getOtherPlugins().getWorldEditPlugin();
        World world = BukkitAdapter.adapt(getBukkitWorld());
        BlockVector3 blockVector31 = BlockVector3.at(elevatorMovement.getLocationDOWN().getBlockX(), elevatorMovement.getLocationDOWN().getBlockY(), elevatorMovement.getLocationDOWN().getBlockZ());
        BlockVector3 blockVector32 = BlockVector3.at(elevatorMovement.getLocationUP().getBlockX(), elevatorMovement.getLocationUP().getBlockY(), elevatorMovement.getLocationUP().getBlockZ());
        CuboidRegion cuboidRegion = new CuboidRegion(world, blockVector31, blockVector32);
        BlockVector3 blockVector3DIR = BlockVector3.at(0, 1, 0);

        try (EditSession editSession = worldEditPlugin.getWorldEdit().newEditSession(world)) {
            editSession.moveCuboidRegion(cuboidRegion, blockVector3DIR, 1, false, null);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
        elevatorMovement.moveUP();
        locationUpPLUS.add(0, 1, 0);
        locationDownPLUS.add(0, 1, 0);
    }

    protected void goDOWN() {
        WorldEditPlugin worldEditPlugin = this.plugin.getOtherPlugins().getWorldEditPlugin();
        World world = BukkitAdapter.adapt(getBukkitWorld());
        BlockVector3 blockVector31 = BlockVector3.at(elevatorMovement.getLocationDOWN().getBlockX(), elevatorMovement.getLocationDOWN().getBlockY(), elevatorMovement.getLocationDOWN().getBlockZ());
        BlockVector3 blockVector32 = BlockVector3.at(elevatorMovement.getLocationUP().getBlockX(), elevatorMovement.getLocationUP().getBlockY(), elevatorMovement.getLocationUP().getBlockZ());
        CuboidRegion cuboidRegion = new CuboidRegion(world, blockVector31, blockVector32);
        BlockVector3 blockVector3DIR = BlockVector3.at(0, -1, 0);

        try (EditSession editSession = worldEditPlugin.getWorldEdit().newEditSession(world)) {
            editSession.moveCuboidRegion(cuboidRegion, blockVector3DIR, 1, false, null);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
        elevatorMovement.moveDOWN();
        locationUpPLUS.subtract(0, 1, 0);
        locationDownPLUS.subtract(0, 1, 0);
    }

    public void emergencyStop() {
        this.isEmergencyStop = true;
    }

    private void floorDone(FloorObject floorObject, ElevatorStatus elevatorStatus) {
        Map<Location, Material> oldBlocks = new HashMap<>();

        //For door list.
        for (Location location : floorObject.getDoorList()) {
            Block block = location.getBlock().getRelative(BlockFace.UP);
            oldBlocks.put(location, location.getBlock().getType());
            if (!ifDoorThenDoIfNotThenFalse(block, true)) {
                location.getBlock().setType(Material.REDSTONE_BLOCK);
            }
        }

        //For main door.
        oldBlocks.put(floorObject.getMainDoor(), floorObject.getMainDoor().getBlock().getType());
        Block block = floorObject.getMainDoor().getBlock().getRelative(BlockFace.UP);
        if (!ifDoorThenDoIfNotThenFalse(block, true)) {
            floorObject.getMainDoor().getBlock().setType(Material.REDSTONE_BLOCK);
        }

        //Signs
        if (elevatorStatus == ElevatorStatus.UP) {
            for (SignObject signObject : floorObject.getSignList()) signObject.doUpArrow();
        } else if (elevatorStatus == ElevatorStatus.DOWN) {
            for (SignObject signObject : floorObject.getSignList()) signObject.doDownArrow();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (SignObject signObject : floorObject.getSignList()) signObject.clearSign();

                oldBlocks.forEach((k, v) -> {
                    Block block = floorObject.getMainDoor().getBlock().getRelative(BlockFace.UP);
                    if (!ifDoorThenDoIfNotThenFalse(block, false)) {
                        k.getBlock().setType(v);
                    }
                });
                isPlayersInItAfter = !getPlayers().isEmpty();
                if (!isPlayersInItAfter) elevatorMessageHelper.stop();
                oldBlocks.clear();
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
            if (this.elevatorMovement.getAtDoor().getBlockY() >= floorObject.getMainDoor().getBlockY())
                this.floorBuffer.remove();
        } else {
            if (this.elevatorMovement.getAtDoor().getBlockY() <= floorObject.getMainDoor().getBlockY())
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

    public void addFloor(FloorObject floorObject) {
        //We have to find out the elevator floor by ourself.
        if (floorObject.getFloor() == Integer.MIN_VALUE) {
            //Checks if the elevator should go up or down.
            boolean goUp = floorObject.getMainDoor().getY() > this.elevatorMovement.getAtDoor().getY();
            int a = goUp ? 1 : -1;
            while (this.getFloorsMap().containsKey(a) && a != 0) {
                if (goUp) a++;
                else a--;
            }
            floorObject.setFloor(a);
            Bukkit.getServer().broadcastMessage("New floor has been set to " + a);
        }

        if (this.floorsMap.get(floorObject.getFloor()) != null) return; //Don't add the floor if we already have it.
        if (floorObject.getFloor() == 0) return; //0 won't be used as a floor anymore.

        if (floorObject.getFloor() >= 1) {
            this.topFloor++;
        } else if (floorObject.getFloor() < 0) {
            this.topBottomFloor--;
        }
        this.floorsMap.put(floorObject.getFloor(), floorObject);
    }

    public void deleteFloor(int floor) {
        if (floor >= 1) this.topFloor--;
        else if (floor < 0) {
            this.topBottomFloor++;
            if (topBottomFloor == 0) this.topBottomFloor++; //0 won't be used as a floor anymore.
        }

        this.floorsMap.remove(floor);
    }

    public FloorObject getFloor(int floor) {
        if (this.floorsMap.containsKey(floor)) {
            return this.floorsMap.get(floor);
        }
        return null;
    }

    public FloorObject getFloor(String name) {
        return this.floorsMap.values().stream().filter(floorObject -> floorObject.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Integer[] listAllFloorsInt() {
        Set<Integer> homeSet = this.floorsMap.keySet();
        Integer[] integers = homeSet.toArray(new Integer[0]);
        Arrays.sort(integers);
        return integers;
    }

    public void clickMessageGoto(Player player) {
        ComponentBuilder componentBuilder = new ComponentBuilder().append("[").color(ChatColor.WHITE).append("BexarSystems").color(ChatColor.GOLD).append(" - ").color(ChatColor.RED).append("Please click a floor in the chat to take the elevator to.").color(ChatColor.BLUE).append("]").color(ChatColor.WHITE).append("\n");
        for (Integer integer : listAllFloorsInt()) {
            FloorObject floorObject = getFloor(integer);
            componentBuilder.append(new ComponentBuilder((floorObject.getName() != null ? floorObject.getName() : String.valueOf(integer)) + ",")
                    .color(ChatColor.GOLD)
                    .bold(true)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elevator call " + elevatorControllerName + " " + elevatorName + " " + (floorObject.getName() != null ? floorObject.getName() : integer)))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click me to go to: " + (floorObject.getName() != null ? floorObject.getName() : integer))))
                    .append(" ")
                    .reset()
                    .create());
        }
        player.spigot().sendMessage(componentBuilder.create());
    }

    private void arrivalChime(Location location) {
        getBukkitWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 10F, 1.8F);
    }

    private void passingChime(Location location) {
        getBukkitWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 10F, 1.3F);
    }

    public static Door isDoor(Location location) {
        Door door = null;
        if (location.getBlock().getType() == Material.IRON_DOOR) {
            door = (Door) location.getBlock().getBlockData();
        }
        return door;
    }

    public boolean ifDoorThenDoIfNotThenFalse(Block block, boolean value) {
        Door door = isDoor(block.getLocation());
        if (door == null) return false;
        door.setOpen(value);
        block.setBlockData(door);
        if (value) block.getWorld().playEffect(block.getLocation(), Effect.IRON_DOOR_TOGGLE, 0);
        else block.getWorld().playEffect(block.getLocation(), Effect.IRON_DOOR_CLOSE, 0);
        return true;
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
        map.put("ShaftPlus", SimpleMath.toBoundingBox(locationDownPLUS.toVector(), locationUpPLUS.toVector()));
        map.put("FloorMap", this.floorsMap);
        return map;
    }

    public static ElevatorObject deserialize(Map<String, Object> map) {
        return new ElevatorObject(World16Elevators.getInstance(), (String) map.get("Name"), (String) map.get("World"), (ElevatorMovement) map.get("Shaft"), (ElevatorSettings) map.get("Settings"), (BoundingBox) map.get("ShaftPlus"), (Map<Integer, FloorObject>) map.get("FloorMap"));
    }
}