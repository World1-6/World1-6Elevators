package com.andrew121410.mc.world16elevators.utils;

import com.andrew121410.mc.world16elevators.objects.ElevatorController;

import java.util.HashMap;
import java.util.Map;

public class SetListMap {

    private Map<String, ElevatorController> elevatorControllerMap;

    public SetListMap(){
        this.elevatorControllerMap = new HashMap<>();
    }

    public Map<String, ElevatorController> getElevatorControllerMap() {
        return elevatorControllerMap;
    }
}
