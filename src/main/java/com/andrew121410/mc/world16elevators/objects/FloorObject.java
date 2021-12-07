package com.andrew121410.mc.world16elevators.objects;

import com.andrew121410.mc.world16elevators.World16Elevators;
import com.andrew121410.mc.world16utils.blocks.BlockUtils;
import com.andrew121410.mc.world16utils.blocks.UniversalBlockUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
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
    private Location blockUnderMainDoor;
    private List<Location> doorList;
    private List<SignObject> signList;

    //Do not save
    private Map<Location, Material> oldBlocks = new HashMap<>();

    public FloorObject(int floor, String name, Location blockUnderMainDoor, List<Location> doorList, List<SignObject> signList) {
        this.floor = floor;
        this.name = name;
        this.blockUnderMainDoor = ifIronDoorThenGetBlockUnderTheDoorIfNotThanReturn(blockUnderMainDoor).getLocation();
        this.doorList = doorList;
        this.signList = signList;
    }

    public FloorObject(int floor, String name, Location blockUnderMainDoor) {
        this(floor, name, blockUnderMainDoor, new ArrayList<>(), new ArrayList<>());
    }

    public FloorObject(int floor, Location blockUnderMainDoor) {
        this(floor, null, blockUnderMainDoor, new ArrayList<>(), new ArrayList<>());
    }

    public FloorObject(String name, Location blockUnderMainDoor) {
        this(Integer.MIN_VALUE, name, blockUnderMainDoor, new ArrayList<>(), new ArrayList<>());
    }

    //Do not remove unnecessary bounding and .clone().
    public static FloorObject from(ElevatorMovement elevatorMovement) {
        return new FloorObject(elevatorMovement.getFloor().intValue(), elevatorMovement.getAtDoor().clone());
    }

    public void doDoor(boolean open, boolean forAllDoors) {
        if (open) {
            //Before
            this.oldBlocks.put(this.blockUnderMainDoor, this.blockUnderMainDoor.getBlock().getType());
            if (forAllDoors)
                for (Location location : this.doorList) this.oldBlocks.put(location, location.getBlock().getType());
        }

        //Main door
        if (!ifIronDoorThenSetOpenIfNotThenFalse(this.getBlockUnderMainDoor().getBlock().getRelative(BlockFace.UP), open)) {
            if (open) this.blockUnderMainDoor.getBlock().setType(Material.REDSTONE_BLOCK);
            else this.blockUnderMainDoor.getBlock().setType(this.oldBlocks.get(this.blockUnderMainDoor));
        }

        //For all the other doors
        if (forAllDoors) {
            for (Location location : this.doorList) {
                Block block = location.getBlock().getRelative(BlockFace.UP);
                if (!ifIronDoorThenSetOpenIfNotThenFalse(block, open)) {
                    if (open) location.getBlock().setType(Material.REDSTONE_BLOCK);
                    else location.getBlock().setType(this.oldBlocks.get(this.blockUnderMainDoor));
                }
            }
        }

        if (!open) this.oldBlocks.clear();
    }

    public void doSigns(ElevatorObject elevatorObject, ElevatorStatus elevatorStatus, boolean revert) {
        if ((this.signList.isEmpty() && !revert) && elevatorObject.getElevatorSettings().isSignFinderSystem()) {
            BlockUtils blockUtils = World16Elevators.getInstance().getOtherPlugins().getWorld16Utils().getClassWrappers().getBlockUtils();

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
        return map;
    }

    public static FloorObject deserialize(Map<String, Object> map) {
        return new FloorObject((int) map.get("Floor"), (String) map.get("Name"), (Location) map.get("MainDoor"), (List<Location>) map.get("DoorList"), (List<SignObject>) map.get("SignList"));
    }
}