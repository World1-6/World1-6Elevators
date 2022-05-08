package com.andrew121410.mc.world16elevators.listeners;

import com.andrew121410.mc.world16elevators.World16Elevators;
import com.andrew121410.mc.world16elevators.objects.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class OnPlayerInteractEvent implements Listener {

    private final World16Elevators plugin;

    public OnPlayerInteractEvent(World16Elevators plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && Tag.BUTTONS.isTagged(event.getClickedBlock().getType())) {
            if (event.getItem() != null && Tag.BUTTONS.isTagged(event.getItem().getType())) return;

            for (int x = -2; x < 3; x++) {
                for (int z = -2; z < 3; z++) {
                    Location blockLocation = event.getClickedBlock().getRelative(x, 0, z).getLocation();
                    Door door = FloorObject.isIronDoor(blockLocation);
                    if (door != null) {
                        Location blockUnderTheDoor = FloorObject.ifIronDoorThenGetBlockUnderTheDoorIfNotThanReturn(blockLocation).getLocation();
                        ElevatorKey elevatorKey = findElevatorKey(blockUnderTheDoor);
                        if (elevatorKey == null) return;

                        // This isn't the main door
                        if (!(blockUnderTheDoor.equals(elevatorKey.getBlockUnderMainDoor()))) {
                            continue;
                        }

                        event.setCancelled(true);

                        if ((x == -2 || x == 2) || (z == -2 || z == 2)) {
                            if (door.getHalf() == Bisected.Half.TOP) {
                                elevatorKey.getElevatorController().callElevatorClosest(event.getPlayer(), elevatorKey.getFloorObject().getName(), ElevatorStatus.UP, ElevatorWho.BUTTON);
                            } else {
                                elevatorKey.getElevatorController().callElevatorClosest(event.getPlayer(), elevatorKey.getFloorObject().getName(), ElevatorStatus.DOWN, ElevatorWho.BUTTON);
                            }
                        } else {
                            if (door.getHalf() == Bisected.Half.TOP) {
                                elevatorKey.getElevatorObject().goToFloor(event.getPlayer(), elevatorKey.getFloorObject().getName(), ElevatorStatus.UP, ElevatorWho.BUTTON);
                            } else {
                                elevatorKey.getElevatorObject().goToFloor(event.getPlayer(), elevatorKey.getFloorObject().getName(), ElevatorStatus.DOWN, ElevatorWho.BUTTON);
                            }
                        }
                        return;
                    }
                }
            }
        }
    }

    private ElevatorKey findElevatorKey(Location blockUnderTheDoor) {
        for (ElevatorController elevatorController : this.plugin.getSetListMap().getElevatorControllerMap().values()) {
            for (ElevatorObject elevatorObject : elevatorController.getElevatorsMap().values()) {
                if (!elevatorObject.getElevatorSettings().isCallButtonSystem()) continue;
                for (FloorObject floorObject : elevatorObject.getFloorsMap().values()) {
                    if (floorObject.getBlockUnderMainDoor().equals(blockUnderTheDoor)) {
                        return new ElevatorKey(elevatorController, elevatorObject, floorObject, floorObject.getBlockUnderMainDoor());
                    }
                    Optional<Location> foundWithOtherDoor = floorObject.getDoorList().stream().filter(location -> location.equals(blockUnderTheDoor)).findFirst();
                    if (foundWithOtherDoor.isPresent()) {
                        return new ElevatorKey(elevatorController, elevatorObject, floorObject, floorObject.getBlockUnderMainDoor());
                    }
                }
            }
        }
        return null;
    }
}

@Getter
@Setter
@AllArgsConstructor
class ElevatorKey {
    private ElevatorController elevatorController;
    private ElevatorObject elevatorObject;
    private FloorObject floorObject;
    private Location blockUnderMainDoor;
}