package com.andrew121410.mc.world16elevators;

import com.andrew121410.mc.world16elevators.enums.ElevatorCallButtonType;
import com.andrew121410.mc.world16elevators.enums.ElevatorFloorSelectorType;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.objectmapping.ConfigSerializable;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.objectmapping.meta.Setting;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@ConfigSerializable
public class ElevatorSettings {

    public static final long DEFAULT_TICKS_PER_SECOND = 6L;
    public static final long DEFAULT_DOOR_HOLDER_TICKS_PER_SECOND = 20L * 5L;
    public static final long DEFAULT_ELEVATOR_WAITER_TICKS_PER_SECOND = 20L * 6L;

    //Config
    @Setting(value = "TicksPerSecond")
    private long ticksPerSecond;
    @Setting(value = "DoorHolderTicksPerSecond")
    private long doorHolderTicksPerSecond;
    @Setting(value = "ElevatorWaiterTicksPerSecond")
    private long elevatorWaiterTicksPerSecond;
    @Setting(value = "DoElevatorLeveling")
    private boolean doElevatorLeveling;
    @Setting(value = "OnlyTwoFloors")
    private boolean onlyTwoFloors;
    @Setting(value = "ArrivalSound")
    private ElevatorSound arrivalSound;
    @Setting(value = "PassingByFloorSound")
    private ElevatorSound passingByFloorSound;
    @Setting(value = "FloorSelectorType")
    private ElevatorFloorSelectorType floorSelectorType;
    @Setting(value = "CallButtonType")
    private ElevatorCallButtonType callButtonType;
    @Setting(value = "SignFinderSystem")
    private boolean signFinderSystem;
    @Setting(value = "TeleportElevatorOnEmpty")
    private boolean teleportElevatorOnEmpty;
    //...

    public ElevatorSettings(long ticksPerSecond,
                            long doorHolderTicksPerSecond,
                            long elevatorWaiterTicksPerSecond,
                            boolean doElevatorLeveling,
                            boolean onlyTwoFloors,
                            ElevatorSound arrivalSound,
                            ElevatorSound passingByFloorSound,
                            ElevatorFloorSelectorType floorSelectorType,
                            ElevatorCallButtonType callButtonType,
                            boolean signFinderSystem,
                            boolean teleportElevatorOnEmpty) {
        this.ticksPerSecond = ticksPerSecond;
        this.doorHolderTicksPerSecond = doorHolderTicksPerSecond;
        this.elevatorWaiterTicksPerSecond = elevatorWaiterTicksPerSecond;
        this.doElevatorLeveling = doElevatorLeveling;
        this.onlyTwoFloors = onlyTwoFloors;
        this.arrivalSound = arrivalSound;
        this.passingByFloorSound = passingByFloorSound;
        this.floorSelectorType = floorSelectorType;
        this.callButtonType = callButtonType;
        this.signFinderSystem = signFinderSystem;
        this.teleportElevatorOnEmpty = teleportElevatorOnEmpty;
    }

    public ElevatorSettings() {
        this(
                DEFAULT_TICKS_PER_SECOND,
                DEFAULT_DOOR_HOLDER_TICKS_PER_SECOND,
                DEFAULT_ELEVATOR_WAITER_TICKS_PER_SECOND,
                true,
                false,
                null,
                null,
                ElevatorFloorSelectorType.CLICK_CHAT,
                ElevatorCallButtonType.CALL_THE_ELEVATOR,
                true,
                false);
    }

    public ElevatorSettings clone() {
        return new ElevatorSettings(
                this.ticksPerSecond,
                this.doorHolderTicksPerSecond,
                this.elevatorWaiterTicksPerSecond,
                this.doElevatorLeveling,
                this.onlyTwoFloors,
                this.arrivalSound,
                this.passingByFloorSound,
                this.floorSelectorType,
                this.callButtonType,
                this.signFinderSystem,
                this.teleportElevatorOnEmpty
        );
    }
}
