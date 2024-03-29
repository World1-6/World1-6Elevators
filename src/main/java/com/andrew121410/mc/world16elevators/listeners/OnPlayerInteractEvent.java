package com.andrew121410.mc.world16elevators.listeners;

import com.andrew121410.mc.world16elevators.ElevatorController;
import com.andrew121410.mc.world16elevators.Elevator;
import com.andrew121410.mc.world16elevators.ElevatorFloor;
import com.andrew121410.mc.world16elevators.World16Elevators;
import com.andrew121410.mc.world16elevators.enums.ElevatorCallButtonType;
import com.andrew121410.mc.world16elevators.enums.ElevatorStatus;
import com.andrew121410.mc.world16elevators.enums.ElevatorWho;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
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
                for (int y = -2; y < 3; y++) {
                    for (int z = -2; z < 3; z++) {
                        Location blockLocation = event.getClickedBlock().getRelative(x, y, z).getLocation();

                        ElevatorKey elevatorKey = findElevatorKey(blockLocation);
                        if (elevatorKey == null) continue;

                        event.setCancelled(true);

                        ElevatorCallButtonType elevatorCallButtonType = elevatorKey.getElevator().getElevatorSettings().getCallButtonType();

                        if (elevatorCallButtonType == ElevatorCallButtonType.CALL_THE_NEAREST_ELEVATOR) {
                            if (Tag.BUTTONS.isTagged(event.getClickedBlock().getRelative(BlockFace.DOWN).getType())) {
                                elevatorKey.getElevatorController().callElevatorClosest(event.getPlayer(), elevatorKey.getElevatorFloor().getName(), ElevatorStatus.UP, ElevatorWho.BUTTON);
                            } else {
                                elevatorKey.getElevatorController().callElevatorClosest(event.getPlayer(), elevatorKey.getElevatorFloor().getName(), ElevatorStatus.DOWN, ElevatorWho.BUTTON);
                            }
                        } else if (elevatorCallButtonType == ElevatorCallButtonType.CALL_THE_ELEVATOR) {
                            if (Tag.BUTTONS.isTagged(event.getClickedBlock().getRelative(BlockFace.DOWN).getType())) {
                                elevatorKey.getElevator().goToFloor(event.getPlayer(), elevatorKey.getElevatorFloor().getName(), ElevatorStatus.UP, ElevatorWho.BUTTON);
                            } else {
                                elevatorKey.getElevator().goToFloor(event.getPlayer(), elevatorKey.getElevatorFloor().getName(), ElevatorStatus.DOWN, ElevatorWho.BUTTON);
                            }
                        }
                        return;
                    }
                }
            }
        }
    }

    private ElevatorKey findElevatorKey(Location blockUnderTheDoor) {
        for (ElevatorController elevatorController : this.plugin.getMemoryHolder().getElevatorControllerMap().values()) {
            for (Elevator elevator : elevatorController.getElevatorsMap().values()) {
                if (elevator.getElevatorSettings().getCallButtonType() == ElevatorCallButtonType.OFF) continue;
                for (ElevatorFloor elevatorFloor : elevator.getFloorsMap().values()) {
                    if (elevatorFloor.getBlockUnderMainDoor().equals(blockUnderTheDoor)) {
                        return new ElevatorKey(elevatorController, elevator, elevatorFloor, elevatorFloor.getBlockUnderMainDoor());
                    }
                    Optional<Location> foundWithOtherDoor = elevatorFloor.getDoorList().stream().filter(location -> location.equals(blockUnderTheDoor)).findFirst();
                    if (foundWithOtherDoor.isPresent()) {
                        return new ElevatorKey(elevatorController, elevator, elevatorFloor, elevatorFloor.getBlockUnderMainDoor());
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
    private Elevator elevator;
    private ElevatorFloor elevatorFloor;
    private Location blockUnderMainDoor;
}