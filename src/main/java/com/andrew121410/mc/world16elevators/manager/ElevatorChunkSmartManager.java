package com.andrew121410.mc.world16elevators.manager;

import com.andrew121410.mc.world16elevators.Main;
import com.andrew121410.mc.world16elevators.objects.ElevatorController;
import org.bukkit.Location;

import java.util.Iterator;
import java.util.Map;

public class ElevatorChunkSmartManager implements Runnable {

    private Map<Location, String> chunksToControllerNameMap;
    private Map<String, ElevatorController> elevatorControllerMap;

    private Main plugin;

    public ElevatorChunkSmartManager(Main plugin) {
        this.plugin = plugin;
        this.chunksToControllerNameMap = this.plugin.getSetListMap().getChunksToControllerNameMap();
        this.elevatorControllerMap = this.plugin.getSetListMap().getElevatorControllerMap();
    }

    @Override
    public void run() {
        Iterator<Map.Entry<Location, String>> iterator = this.chunksToControllerNameMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Location, String> entry = iterator.next();
            Location location = entry.getKey();
            String controllerName = entry.getValue();
            boolean isChunkLoaded = location.getWorld().isChunkLoaded(location.getBlockX(), location.getBlockZ());
            if (isChunkLoaded && !this.elevatorControllerMap.containsKey(controllerName)) {
                this.plugin.getElevatorManager().loadElevatorController(controllerName);
            } else if (!isChunkLoaded && this.elevatorControllerMap.containsKey(controllerName)) {
                ElevatorController elevatorController = this.elevatorControllerMap.get(controllerName);
                long numberOfElevatorsThatAreRunning = elevatorController.getElevatorsMap().entrySet().stream().filter(entry1 -> entry1.getValue().isGoing()).count();
                //It's safe to remove the elevator controller because all elevators in the elevator controller aren't running.
                if (numberOfElevatorsThatAreRunning == 0) {
                    this.plugin.getElevatorManager().saveAndUnloadElevatorController(elevatorController);
                }
            }
        }
    }
}
