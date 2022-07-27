package com.andrew121410.mc.world16elevators.storage;

import com.andrew121410.mc.world16elevators.World16Elevators;
import com.andrew121410.mc.world16elevators.ElevatorController;
import com.andrew121410.mc.world16utils.chat.Translate;
import com.andrew121410.mc.world16utils.config.CustomYmlManager;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public class ElevatorManager {

    private Map<Location, String> chunksToControllerNameMap;
    private Map<String, ElevatorController> elevatorControllerMap;

    private World16Elevators plugin;
    private CustomYmlManager elevatorsYml;

    public ElevatorManager(World16Elevators plugin) {
        this.plugin = plugin;
        this.chunksToControllerNameMap = this.plugin.getSetListMap().getChunksToControllerNameMap();
        this.elevatorControllerMap = this.plugin.getSetListMap().getElevatorControllerMap();
        //elevators.yml
        this.elevatorsYml = new CustomYmlManager(this.plugin, false);
        this.elevatorsYml.setup("elevators.yml");
        this.elevatorsYml.saveConfig();
        this.elevatorsYml.reloadConfig();
        //...
    }

    public void loadAllElevators() {
        //This runs when elevator.yml is first created.
        ConfigurationSection elevatorControllersSection = this.elevatorsYml.getConfig().getConfigurationSection("ElevatorControllers");
        if (elevatorControllersSection == null) {
            this.elevatorsYml.getConfig().createSection("ElevatorControllers");
            this.elevatorsYml.saveConfig();
            this.plugin.getServer().getConsoleSender().sendMessage(Translate.chat("&c[ElevatorManager]&r&6 ElevatorControllers section has been created."));
            return; //Return because don't try to load nothing.
        }
        for (String elevatorControllerName : elevatorControllersSection.getKeys(false)) {
            ElevatorController elevatorController = (ElevatorController) elevatorControllersSection.get(elevatorControllerName);
            if (this.plugin.isChunkSmartManagement())
                this.chunksToControllerNameMap.put(elevatorController.getMainChunk(), elevatorControllerName);
            else this.elevatorControllerMap.put(elevatorControllerName.toLowerCase(), elevatorController);
        }
    }

    public void saveAllElevators() {
        ConfigurationSection elevatorControllersSection = this.elevatorsYml.getConfig().getConfigurationSection("ElevatorControllers");
        if (elevatorControllersSection == null) {
            elevatorControllersSection = this.elevatorsYml.getConfig().createSection("ElevatorControllers");
            this.elevatorsYml.saveConfig();
        }
        //For each elevator controller.
        for (Map.Entry<String, ElevatorController> mapEntry : this.elevatorControllerMap.entrySet()) {
            String controllerName = mapEntry.getKey();
            ElevatorController elevatorController = mapEntry.getValue();
            elevatorControllersSection.set(controllerName, elevatorController);
            this.elevatorsYml.saveConfig();
        }
    }

    public ElevatorController loadElevatorController(String key) {
        ConfigurationSection elevatorControllersSection = this.elevatorsYml.getConfig().getConfigurationSection("ElevatorControllers");
        if (elevatorControllersSection == null) {
            elevatorControllersSection = this.elevatorsYml.getConfig().createSection("ElevatorControllers");
            this.elevatorsYml.saveConfig();
        }
        ElevatorController elevatorController = (ElevatorController) elevatorControllersSection.get(key);
        this.elevatorControllerMap.putIfAbsent(key, elevatorController);
        return elevatorController;
    }

    public void saveAndUnloadElevatorController(ElevatorController elevatorController) {
        ConfigurationSection elevatorControllersSection = this.elevatorsYml.getConfig().getConfigurationSection("ElevatorControllers");
        if (elevatorControllersSection == null) {
            elevatorControllersSection = this.elevatorsYml.getConfig().createSection("ElevatorControllers");
            this.elevatorsYml.saveConfig();
        }
        elevatorControllersSection.set(elevatorController.getControllerName(), elevatorController);
        this.elevatorControllerMap.remove(elevatorController.getControllerName());
        this.elevatorsYml.saveConfig();
    }

    public void deleteElevatorController(String name) {
        ElevatorController elevatorController = this.elevatorControllerMap.get(name.toLowerCase());
        if (elevatorController.getMainChunk() != null) {
            this.chunksToControllerNameMap.remove(elevatorController.getMainChunk());
        }
        this.elevatorControllerMap.remove(name.toLowerCase());
        ConfigurationSection elevatorControllersSection = this.elevatorsYml.getConfig().getConfigurationSection("ElevatorControllers");
        if (elevatorControllersSection == null) return;
        elevatorControllersSection.set(name.toLowerCase(), null);
        this.elevatorsYml.saveConfig();
    }

    public void deleteElevator(String elevatorControllerName, String elevatorName) {
        ElevatorController elevatorController = this.elevatorControllerMap.get(elevatorControllerName);
        if (elevatorController == null) return;
        elevatorController.getElevatorsMap().remove(elevatorName);
        ConfigurationSection elevatorsSection = this.elevatorsYml.getConfig().getConfigurationSection("ElevatorControllers." + elevatorControllerName.toLowerCase() + ".ElevatorMap");
        if (elevatorsSection == null) return;
        elevatorsSection.set(elevatorName, null);
        this.elevatorsYml.saveConfig();
    }
}