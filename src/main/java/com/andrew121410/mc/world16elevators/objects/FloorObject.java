package com.andrew121410.mc.world16elevators.objects;

import com.andrew121410.mc.world16utils.blocks.UniversalBlockUtils;
import lombok.*;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.GlassPane;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@SerializableAs("FloorObject")
public class FloorObject implements ConfigurationSerializable {

    private int floor;
    private String name;
    private Location blockUnderMainDoor;
    private List<Location> doorList;
    private List<SignObject> signList;
    private String permission;

    //Do not save
    private Map<Location, SavedBlock> oldBlocks = new HashMap<>();

    public FloorObject(int floor, String name, Location blockUnderMainDoor, List<Location> doorList, List<SignObject> signList, String permission) {
        this.floor = floor;
        this.name = name;
        this.blockUnderMainDoor = ifIronDoorThenGetBlockUnderTheDoorIfNotThanReturn(blockUnderMainDoor).getLocation();
        this.doorList = doorList;
        this.signList = signList;
        this.permission = permission;
    }

    public FloorObject(int floor, String name, Location blockUnderMainDoor) {
        this(floor, name, blockUnderMainDoor, new ArrayList<>(), new ArrayList<>(), null);
    }

    public FloorObject(int floor, Location blockUnderMainDoor) {
        this(floor, null, blockUnderMainDoor, new ArrayList<>(), new ArrayList<>(), null);
    }

    public FloorObject(String name, Location blockUnderMainDoor) {
        this(Integer.MIN_VALUE, name, blockUnderMainDoor, new ArrayList<>(), new ArrayList<>(), null);
    }

    //Do not remove unnecessary bounding and .clone().
    public static FloorObject from(ElevatorMovement elevatorMovement) {
        return new FloorObject(elevatorMovement.getFloor().intValue(), elevatorMovement.getAtDoor().clone());
    }

    public void doSigns(ElevatorObject elevatorObject, ElevatorStatus elevatorStatus, boolean revert) {
        if ((this.signList.isEmpty() && !revert) && elevatorObject.getElevatorSettings().isSignFinderSystem()) {
            List<Sign> signs = new ArrayList<>();

            //Find signs
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    Location signLocation = this.getBlockUnderMainDoor().getBlock().getRelative(0, 3, 0).getRelative(x, 0, z).getLocation();
                    Sign sign = UniversalBlockUtils.isSign(signLocation.getBlock());
                    if (sign != null) signs.add(sign);
                }
            }

            //Couldn't find any signs
            if (signs.isEmpty()) return;

            this.signList.add(new SignObject(signs.stream().findAny().get().getLocation()));
        }

        if (revert) {
            this.signList.removeIf(signObject -> !signObject.revert());
            return;
        }

        switch (elevatorStatus) {
            case UP -> this.signList.removeIf(signObject -> !signObject.doUpArrow());
            case DOWN -> this.signList.removeIf(signObject -> !signObject.doDownArrow());
        }
    }

    public void doDoor(boolean open, boolean forAllDoors) {
        handleDoor(this.blockUnderMainDoor.getBlock(), open);

        if (forAllDoors) {
            for (Location location : this.doorList) {
                handleDoor(location.getBlock(), open);
            }
        }

        if (!open) this.oldBlocks.clear();
    }

    private void handleDoor(Block block, boolean open) {
        Block openableAbove = block.getRelative(BlockFace.UP);
        Block openableAbove2 = openableAbove.getRelative(BlockFace.UP);

        if (!ifIronDoorThenSetOpenIfNotThenFalse(openableAbove, open)) {
            if (open) {
                // Ran when it's not an iron door & open is true, so we are going to check if this is some sort of other door,
                // like a door made out of glass, or some sort of other Openable
                // if not then just set the block that was passed into the function to redstone
                if (openableAbove.getType() == Material.GLASS || openableAbove.getType() == Material.GLASS_PANE || openableAbove.getBlockData() instanceof GlassPane) {
                    this.oldBlocks.putIfAbsent(openableAbove.getLocation(), new SavedBlock(openableAbove.getType(), openableAbove.getBlockData()));
                    openableAbove.setType(Material.AIR);
                    openableAbove.getWorld().playSound(openableAbove.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);

                    // If there is 2 glass pane then also remove it
                    if (openableAbove2.getType() == Material.GLASS || openableAbove2.getType() == Material.GLASS_PANE || openableAbove2.getBlockData() instanceof GlassPane) {
                        this.oldBlocks.putIfAbsent(openableAbove2.getLocation(), new SavedBlock(openableAbove2.getType(), openableAbove2.getBlockData()));
                        openableAbove2.setType(Material.AIR);
                        openableAbove.getWorld().playSound(openableAbove2.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
                    }
                } else if (openableAbove.getBlockData() instanceof Openable realOpenable) {
                    realOpenable.setOpen(true);
                    openableAbove.setBlockData(realOpenable);
                } else {
                    this.oldBlocks.putIfAbsent(block.getLocation(), new SavedBlock(block.getType(), null));
                    block.setType(Material.REDSTONE_BLOCK);
                }
            }
        }

        if (!open) {
            SavedBlock savedBlock;
            if ((savedBlock = this.oldBlocks.getOrDefault(block.getLocation(), null)) != null) {
                block.setType(savedBlock.getMaterial());
            } else if ((savedBlock = this.oldBlocks.getOrDefault(openableAbove.getLocation(), null)) != null) {
                openableAbove.setType(savedBlock.getMaterial());
                openableAbove.setBlockData(savedBlock.getBlockData());
                // See if there is also something above also
                if ((savedBlock = this.oldBlocks.getOrDefault(openableAbove2.getLocation(), null)) != null) {
                    openableAbove2.setType(savedBlock.getMaterial());
                    openableAbove2.setBlockData(savedBlock.getBlockData());
                }
            } else {
                if (openableAbove.getBlockData() instanceof Openable realOpenable) {
                    realOpenable.setOpen(false);
                    openableAbove.setBlockData(realOpenable);
                }
            }
        }
    }

    public static Door isIronDoor(Location location) {
        Door door = null;
        if (location.getBlock().getType() == Material.IRON_DOOR) {
            door = (Door) location.getBlock().getBlockData();
        }
        return door;
    }

    public static boolean ifIronDoorThenSetOpenIfNotThenFalse(Block block, boolean value) {
        Door door = isIronDoor(block.getLocation());
        if (door == null) return false;
        door.setOpen(value);
        block.setBlockData(door);
        if (value) block.getWorld().playEffect(block.getLocation(), Effect.IRON_DOOR_TOGGLE, 0);
        else block.getWorld().playEffect(block.getLocation(), Effect.IRON_DOOR_CLOSE, 0);
        return true;
    }

    public static Block ifIronDoorThenGetBlockUnderTheDoorIfNotThanReturn(Location location) {
        Door door = isIronDoor(location);
        if (door != null) {
            if (door.getHalf() == Bisected.Half.TOP) {
                return location.getBlock().getRelative(0, -2, 0);
            } else {
                return location.getBlock().getRelative(0, -1, 0);
            }
        }
        return location.getBlock();
    }

    public String getName() {
        return name != null ? name : String.valueOf(floor);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("Floor", this.floor);
        map.put("Name", this.name);
        map.put("MainDoor", this.blockUnderMainDoor);
        map.put("DoorList", this.doorList);
        map.put("SignList", this.signList);
        map.put("Permission", this.permission);
        return map;
    }

    public static FloorObject deserialize(Map<String, Object> map) {
        return new FloorObject((int) map.get("Floor"), (String) map.get("Name"), (Location) map.get("MainDoor"), (List<Location>) map.get("DoorList"), (List<SignObject>) map.get("SignList"), (String) map.get("Permission"));
    }
}

@Getter
@AllArgsConstructor
class SavedBlock {
    private Material material;
    private BlockData blockData;
}