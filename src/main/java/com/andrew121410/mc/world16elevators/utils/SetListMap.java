package com.andrew121410.mc.world16elevators.utils;

import com.andrew121410.mc.world16elevators.objects.ElevatorController;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class SetListMap {

    private Map<Location, String> chunksToControllerNameMap;
    private Map<String, ElevatorController> elevatorControllerMap;

    public SetListMap() {
        this.chunksToControllerNameMap = new HashMap<>();
        this.elevatorControllerMap = new HashMap<>();
    }

    public Map<String, ElevatorController> getElevatorControllerMap() {
        return elevatorControllerMap;
    }

    public Map<Location, String> getChunksToControllerNameMap() {
        return chunksToControllerNameMap;
    }
}
