package com.andrew121410.mc.world16elevators;

import com.andrew121410.mc.world16utils.gui.GUIWindow;
import com.andrew121410.mc.world16utils.gui.buttons.AbstractGUIButton;
import com.andrew121410.mc.world16utils.gui.buttons.defaults.ClickEventButton;
import com.andrew121410.mc.world16utils.utils.InventoryUtils;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class GUIElevatorFloors extends GUIWindow {

    private World16Elevators plugin;
    private ElevatorObject elevatorObject;

    @Override
    public void onCreate(Player player) {
        List<AbstractGUIButton> guiButtons = new ArrayList<>();
        int guiSlots = elevatorObject.getFloorsMap().size() + (9 - (elevatorObject.getFloorsMap().size() % 9));

        int slot = 0;
        for (Map.Entry<Integer, FloorObject> entry : elevatorObject.getFloorsMap().entrySet()) {
            Integer floor = entry.getKey();
            FloorObject floorObject = entry.getValue();
            guiButtons.add(new ClickEventButton(slot, InventoryUtils.createItem(Material.GREEN_CONCRETE, 1, floorObject.getName(), "Click to go to floor."), (guiClickEvent -> {
                this.plugin.getServer().dispatchCommand(player, "elevator call " + elevatorObject.getElevatorControllerName() + " " + elevatorObject.getElevatorName() + " " + floorObject.getName());
                player.closeInventory();
            })));
            slot++;
        }
        this.update(guiButtons, "Elevator Floors!", guiSlots);
    }

    @Override
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {

    }
}
