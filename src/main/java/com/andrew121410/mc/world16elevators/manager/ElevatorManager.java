package com.andrew121410.mc.world16elevators.manager;

import com.andrew121410.mc.world16elevators.Main;
import com.andrew121410.mc.world16elevators.objects.ElevatorController;
import com.andrew121410.mc.world16elevators.objects.ElevatorObject;
import com.andrew121410.mc.world16utils.chat.Translate;
import com.andrew121410.mc.world16utils.config.CustomYmlManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public class ElevatorManager {

    private Map<String, ElevatorController> elevatorObjectMap;

    private Main plugin;
    private CustomYmlManager elevatorsYml;

    public ElevatorManager(Main plugin) {
        this.plugin = plugin;
        this.elevatorObjectMap = this.plugin.getSetListMap().getElevatorControllerMap();
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
            ConfigurationSection elevatorControllerSection = elevatorControllersSection.getConfigurationSection(elevatorControllerName);
            ElevatorController elevatorController = (ElevatorController) elevatorControllerSection.get("ElevatorController");
            this.elevatorObjectMap.put(elevatorControllerName.toLowerCase(), elevatorController);
        }
    }

    public void saveAllElevators() {
        ConfigurationSection elevatorControllersSection = this.elevatorsYml.getConfig().getConfigurationSection("ElevatorControllers");
        if (elevatorControllersSection == null) {
            elevatorControllersSection = this.elevatorsYml.getConfig().createSection("ElevatorControllers");
            this.elevatorsYml.saveConfig();
        }

        //For each elevator controller.
        for (Map.Entry<String, ElevatorController> mapEntry : this.elevatorObjectMap.entrySet()) {
            String controllerName = mapEntry.getKey();
            ElevatorController elevatorController = mapEntry.getValue();

            //Elevator controller.
            ConfigurationSection elevatorControllerSection = elevatorControllersSection.getConfigurationSection(controllerName);
            if (elevatorControllerSection == null) {
                elevatorControllerSection = elevatorControllersSection.createSection(controllerName);
                this.elevatorsYml.saveConfig();
            }

            elevatorControllerSection.set("ElevatorController", elevatorController);

            this.elevatorsYml.saveConfig();
        }
    }

    public void deleteElevatorController(String name) {
        this.elevatorObjectMap.remove(name.toLowerCase());

        ConfigurationSection elevatorControllersSection = this.elevatorsYml.getConfig().getConfigurationSection("ElevatorControllers");
        if (elevatorControllersSection == null) return;

        elevatorControllersSection.set(name.toLowerCase(), null);
        this.elevatorsYml.saveConfig();
    }

    public void deleteElevator(String elevatorControllerName, String elevatorName) {
        ElevatorController elevatorController = this.elevatorObjectMap.get(elevatorControllerName);
        if (elevatorController == null) return;
        elevatorController.getElevatorsMap().remove(elevatorName);

        ConfigurationSection elevatorsSection = this.elevatorsYml.getConfig().getConfigurationSection("ElevatorControllers." + elevatorControllerName.toLowerCase() + ".Elevators");
        if (elevatorsSection == null) return;

        elevatorsSection.set(elevatorName, null);
        this.elevatorsYml.saveConfig();
    }

    public void deleteFloorOfElevator(String elevatorControllerName, String elevatorName, int floorNum) {
        ElevatorController elevatorController = this.elevatorObjectMap.get(elevatorControllerName);
        if (elevatorController == null) return;
        ElevatorObject elevatorObject = elevatorController.getElevatorsMap().get(elevatorName);
        elevatorObject.deleteFloor(floorNum);

        ConfigurationSection elevatorFloors = this.elevatorsYml.getConfig().getConfigurationSection("ElevatorControllers." + elevatorControllerName.toLowerCase() + ".Elevators." + elevatorName + ".Floors");
        if (elevatorFloors == null) return;

        elevatorFloors.set(String.valueOf(floorNum), null);
        this.elevatorsYml.saveConfig();
    }
}