package com.andrew121410.mc.world16elevators.objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@SerializableAs("ElevatorSettings")
public class ElevatorSettings implements ConfigurationSerializable {

    public static final long DEFAULT_TICKS_PER_SECOND = 6L;
    public static final long DEFAULT_DOOR_HOLDER_TICKS_PER_SECOND = 20L * 5L;
    public static final long DEFAULT_ELEVATOR_WAITER_TICKS_PER_SECOND = 20L * 6L;

    //Config
    private long ticksPerSecond;
    private long doorHolderTicksPerSecond;
    private long elevatorWaiterTicksPerSecond;
    private boolean doElevatorLeveling;
    private boolean onlyTwoFloors;
    private ElevatorSound arrivalSound;
    private ElevatorSound passingByFloorSound;
    private ElevatorCallSystem elevatorCallSystem;
    private Boolean callButtonSystem;
    //...

    public ElevatorSettings(long ticksPerSecond, long doorHolderTicksPerSecond, long elevatorWaiterTicksPerSecond, boolean doElevatorLeveling, boolean onlyTwoFloors, ElevatorSound arrivalSound, ElevatorSound passingByFloorSound, ElevatorCallSystem elevatorCallSystem, Boolean callButtonSystem) {
        this.ticksPerSecond = ticksPerSecond;
        this.doorHolderTicksPerSecond = doorHolderTicksPerSecond;
        this.elevatorWaiterTicksPerSecond = elevatorWaiterTicksPerSecond;
        this.doElevatorLeveling = doElevatorLeveling;
        this.onlyTwoFloors = onlyTwoFloors;
        this.arrivalSound = arrivalSound;
        this.passingByFloorSound = passingByFloorSound;
        this.elevatorCallSystem = elevatorCallSystem;
        this.callButtonSystem = callButtonSystem;
    }

    public ElevatorSettings() {
        this(DEFAULT_TICKS_PER_SECOND, DEFAULT_DOOR_HOLDER_TICKS_PER_SECOND, DEFAULT_ELEVATOR_WAITER_TICKS_PER_SECOND, true, false, null, null, ElevatorCallSystem.CLICK_CHAT, true);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("TicksPerSecond", this.ticksPerSecond);
        map.put("DoorHolderTicksPerSecond", this.doorHolderTicksPerSecond);
        map.put("ElevatorWaiterTicksPerSecond", this.elevatorWaiterTicksPerSecond);
        map.put("DoElevatorLeveling", this.doElevatorLeveling);
        map.put("OnlyTwoFloors", this.onlyTwoFloors);
        map.put("ArrivalSound", this.arrivalSound);
        map.put("PassingByFloorSound", this.passingByFloorSound);
        map.put("ElevatorCallSystem", this.elevatorCallSystem.name());
        map.put("CallButtonSystem", this.callButtonSystem);
        return map;
    }

    public static ElevatorSettings deserialize(Map<String, Object> map) {
        return new ElevatorSettings(((Integer) map.get("TicksPerSecond")).longValue(),
                ((Integer) map.get("DoorHolderTicksPerSecond")).longValue(),
                ((Integer) map.get("ElevatorWaiterTicksPerSecond")).longValue(),
                (Boolean) map.get("DoElevatorLeveling"),
                (Boolean) map.get("OnlyTwoFloors"),
                (ElevatorSound) map.get("ArrivalSound"),
                (ElevatorSound) map.get("PassingByFloorSound"),
                ElevatorCallSystem.valueOf((String) map.get("ElevatorCallSystem")),
                (Boolean) map.get("CallButtonSystem"));
    }
}
