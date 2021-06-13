package com.andrew121410.mc.world16elevators.objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
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
    private Location mainDoor;
    private List<Location> doorList;
    private List<SignObject> signList;

    //Do not save
    private Map<Location, Material> oldBlocks = new HashMap<>();

    public FloorObject(int floor, String name, Location mainDoor, List<Location> doorList, List<SignObject> signList) {
        this.floor = floor;
        this.name = name;
        this.mainDoor = IfIronDoorThenGetBlockUnderTheDoorIfNotThanReturn(mainDoor).getLocation();
        this.doorList = doorList;
        this.signList = signList;
    }

    public FloorObject(int floor, String name, Location mainDoor) {
        this(floor, name, mainDoor, new ArrayList<>(), new ArrayList<>());
    }

    public FloorObject(int floor, Location mainDoor) {
        this(floor, null, mainDoor, new ArrayList<>(), new ArrayList<>());
    }

    public FloorObject(String name, Location mainDoor) {
        this(Integer.MIN_VALUE, name, mainDoor, new ArrayList<>(), new ArrayList<>());
    }

    //Do not remove unnecessary bounding and .clone().
    public static FloorObject from(ElevatorMovement elevatorMovement) {
        return new FloorObject(new Integer(elevatorMovement.getFloor()), elevatorMovement.getAtDoor().clone());
    }

    public void doDoor(boolean open, boolean forAllDoors) {
        if (open) {
            //Before
            this.oldBlocks.put(this.mainDoor, this.mainDoor.getBlock().getType());
            if (forAllDoors)
                for (Location location : this.doorList) this.oldBlocks.put(location, location.getBlock().getType());
        }

        //Main door
        if (!IfIronDoorThenSetOpenIfNotThenFalse(this.getMainDoor().getBlock().getRelative(BlockFace.UP), open)) {
            if (open) this.mainDoor.getBlock().setType(Material.REDSTONE_BLOCK);
            else this.mainDoor.getBlock().setType(this.oldBlocks.get(this.mainDoor));
        }

        //For all the other doors
        if (forAllDoors) {
            for (Location location : this.doorList) {
                Block block = location.getBlock().getRelative(BlockFace.UP);
                if (!IfIronDoorThenSetOpenIfNotThenFalse(block, open)) {
                    if (open) location.getBlock().setType(Material.REDSTONE_BLOCK);
                    else location.getBlock().setType(this.oldBlocks.get(this.mainDoor));
                }
            }
        }

        if (!open) this.oldBlocks.clear();
    }

    public static Door isDoor(Location location) {
        Door door = null;
        if (location.getBlock().getType() == Material.IRON_DOOR) {
            door = (Door) location.getBlock().getBlockData();
        }
        return door;
    }

    public static boolean IfIronDoorThenSetOpenIfNotThenFalse(Block block, boolean value) {
        Door door = isDoor(block.getLocation());
        if (door == null) return false;
        door.setOpen(value);
        block.setBlockData(door);
        if (value) block.getWorld().playEffect(block.getLocation(), Effect.IRON_DOOR_TOGGLE, 0);
        else block.getWorld().playEffect(block.getLocation(), Effect.IRON_DOOR_CLOSE, 0);
        return true;
    }

    public static Block IfIronDoorThenGetBlockUnderTheDoorIfNotThanReturn(Location location) {
        Door door = isDoor(location);
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
        map.put("MainDoor", this.mainDoor);
        map.put("DoorList", this.doorList);
        map.put("SignList", this.signList);
        return map;
    }

    public static FloorObject deserialize(Map<String, Object> map) {
        return new FloorObject((int) map.get("Floor"), (String) map.get("Name"), (Location) map.get("MainDoor"), (List<Location>) map.get("DoorList"), (List<SignObject>) map.get("SignList"));
    }
}