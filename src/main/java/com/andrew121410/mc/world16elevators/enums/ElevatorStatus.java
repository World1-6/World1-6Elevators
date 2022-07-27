package com.andrew121410.mc.world16elevators.enums;

public enum ElevatorStatus {
    UP,
    DOWN,
    DONT_KNOW;

    public static ElevatorStatus upOrDown(boolean up) {
        if (up) return UP;
        return DOWN;
    }
}
