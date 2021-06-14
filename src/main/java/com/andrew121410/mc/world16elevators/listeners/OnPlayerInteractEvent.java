package com.andrew121410.mc.world16elevators.listeners;

import com.andrew121410.mc.world16elevators.World16Elevators;
import com.andrew121410.mc.world16elevators.objects.*;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

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

            //@TODO - clean this shit up to make it look nicer

            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    Location doorLocation = event.getClickedBlock().getRelative(x, 0, z).getLocation();
                    Door door = FloorObject.isIronDoor(doorLocation);
                    if (door != null) {
                        Location blockUnderTheDoor = FloorObject.ifIronDoorThenGetBlockUnderTheDoorIfNotThanReturn(doorLocation).getLocation();
                        for (ElevatorController elevatorController : this.plugin.getSetListMap().getElevatorControllerMap().values()) {
                            for (ElevatorObject elevatorObject : elevatorController.getElevatorsMap().values()) {
                                if (!elevatorObject.getElevatorSettings().isCallButtonSystem()) continue;
                                for (FloorObject floorObject : elevatorObject.getFloorsMap().values()) {
                                    if (floorObject.getBlockUnderMainDoor().equals(blockUnderTheDoor)) {
                                        event.setCancelled(true);
                                        if (door.getHalf() == Bisected.Half.TOP) {
                                            elevatorController.callElevatorClosest(floorObject.getFloor(), ElevatorStatus.UP, ElevatorWho.BUTTON);
                                        } else {
                                            elevatorController.callElevatorClosest(floorObject.getFloor(), ElevatorStatus.DOWN, ElevatorWho.BUTTON);
                                        }
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
