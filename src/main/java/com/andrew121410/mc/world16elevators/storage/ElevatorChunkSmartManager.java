package com.andrew121410.mc.world16elevators.storage;

import com.andrew121410.mc.world16elevators.ElevatorController;
import com.andrew121410.mc.world16elevators.World16Elevators;
import org.bukkit.Location;

import java.util.Iterator;
import java.util.Map;

// The purpose of this class is to load and unload elevator controllers when they are in chunks that are loaded or unloaded.
public class ElevatorChunkSmartManager implements Runnable {

    private final Map<Location, String> chunksToControllerNameMap;
    private final Map<String, ElevatorController> elevatorControllerMap;

    private final World16Elevators plugin;

    public ElevatorChunkSmartManager(World16Elevators plugin) {
        this.plugin = plugin;
        this.chunksToControllerNameMap = this.plugin.getMemoryHolder().getChunksToControllerNameMap();
        this.elevatorControllerMap = this.plugin.getMemoryHolder().getElevatorControllerMap();
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
