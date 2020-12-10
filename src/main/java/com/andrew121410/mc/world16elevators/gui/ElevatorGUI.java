package com.andrew121410.mc.world16elevators.gui;

import com.andrew121410.mc.world16elevators.World16Elevators;
import com.andrew121410.mc.world16elevators.objects.ElevatorController;
import com.andrew121410.mc.world16elevators.objects.ElevatorObject;
import com.andrew121410.mc.world16utils.chat.ChatResponseManager;
import com.andrew121410.mc.world16utils.gui.simple.SimpleGUIItem;
import com.andrew121410.mc.world16utils.gui.simple.SimpleGUIWindow;
import com.andrew121410.mc.world16utils.utils.InventoryUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ElevatorGUI extends SimpleGUIWindow {

    private Map<String, ElevatorController> elevatorControllerMap;
    private Map<Location, String> chunksToControllerNameMap;

    private World16Elevators plugin;

    public ElevatorGUI(World16Elevators plugin) {
        super("&bElevator GUI", 54);
        this.plugin = plugin;
        this.elevatorControllerMap = this.plugin.getSetListMap().getElevatorControllerMap();
        this.chunksToControllerNameMap = this.plugin.getSetListMap().getChunksToControllerNameMap();
        populateElevatorControllers();
    }

    private void populateElevatorControllers() {
        Map<Integer, SimpleGUIItem> simpleGUIItemMap = new HashMap<>();
        int slot = 0;

        for (Map.Entry<Location, String> locationStringEntry : this.chunksToControllerNameMap.entrySet()) {
            Location location = locationStringEntry.getKey();
            String name = locationStringEntry.getValue();
            if (this.elevatorControllerMap.containsKey(name)) {
                simpleGUIItemMap.put(slot, new SimpleGUIItem(slot, InventoryUtils.createItem(Material.GREEN_CONCRETE, 1, name, ""), (event -> {
                    Player player = (Player) event.getWhoClicked();
                    populateElevatorController(player, event.getCurrentItem().getItemMeta().getDisplayName());
                    super.open(player);
                })));
            } else {
                simpleGUIItemMap.put(slot, new SimpleGUIItem(slot, InventoryUtils.createItem(Material.RED_CONCRETE, 1, name, "Controller isn't loaded.", "Click me to tp to it."), (event -> {
                    Player player = (Player) event.getWhoClicked();
                    player.teleport(location);
                })));
            }
            slot++;
        }
        super.update(simpleGUIItemMap, 54);
    }

    private void populateElevatorController(Player player, String controllerName) {
        Map<Integer, SimpleGUIItem> simpleGUIItemMap = new HashMap<>();

        simpleGUIItemMap.put(20, new SimpleGUIItem(20, InventoryUtils.createItem(Material.IRON_BARS, 1, "&6&lElevators", "Click me to show all elevators"), (event -> {
            populateElevatorsMenu(player, controllerName);
            super.open(player);
        })));

        simpleGUIItemMap.put(44, new SimpleGUIItem(44, InventoryUtils.createItem(Material.BARRIER, 1, "&c&lDelete", "Deletes elevator controller"), event -> {
            populateElevatorControllerDelete(player, controllerName);
            super.open(player);
        }));

        super.update(simpleGUIItemMap, 45);
    }

    private void populateElevatorControllerDelete(Player player, String controllerName) {
        Map<Integer, SimpleGUIItem> simpleGUIItemMap = new HashMap<>();

        simpleGUIItemMap.put(0, new SimpleGUIItem(0, InventoryUtils.createItem(Material.BARRIER, 1, "Cancel", "No don't delete."), event -> {
            populateElevatorController(player, controllerName);
            super.open(player);
        }));

        simpleGUIItemMap.put(8, new SimpleGUIItem(8, InventoryUtils.createItem(Material.EMERALD, 1, "Confirm", "Yes, I want to delete!"), event -> {
            player.performCommand("elevator controller delete " + controllerName);
            player.closeInventory();
        }));
        super.update(simpleGUIItemMap, 9);
    }

    private void populateElevatorsMenu(Player player, String controllerName) {
        Map<Integer, SimpleGUIItem> simpleGUIItemMap = new HashMap<>();

        ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
        if (elevatorController == null) {
            throw new NullPointerException("elevatorController is null");
        }
        int slot = 0;

        for (Map.Entry<String, ElevatorObject> entry : elevatorController.getElevatorsMap().entrySet()) {
            String elevatorName = entry.getKey();
            ElevatorObject elevatorObject = entry.getValue();
            simpleGUIItemMap.put(slot, new SimpleGUIItem(slot, InventoryUtils.createItem(Material.CHAIN, 1, elevatorName, "Click me to see elevator settings."), (event -> {
                populateElevatorMenu(player, elevatorController, elevatorObject);
                super.open(player);
            })));
            slot++;
        }
        super.update(simpleGUIItemMap, 54);
    }

    private void populateElevatorMenu(Player player, ElevatorController elevatorController, ElevatorObject elevatorObject) {
        Map<Integer, SimpleGUIItem> simpleGUIItemMap = new HashMap<>();

        simpleGUIItemMap.put(0, new SimpleGUIItem(0, InventoryUtils.createItem(Material.WHITE_WOOL, 1, "ticksPerSecond", ""), (event -> {
            ChatResponseManager chatResponseManager = this.plugin.getOtherPlugins().getWorld16Utils().getChatResponseManager();
            chatResponseManager.create(player, null, null, (player1, message) -> {
                player1.performCommand("elevator settings " + elevatorController.getControllerName() + " " + elevatorObject.getElevatorName() + " " + "ticksPerSecond " + message);
            });
        })));
        super.update(simpleGUIItemMap, 9);
    }
}
