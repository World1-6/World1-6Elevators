package com.andrew121410.mc.world16elevators;

import com.andrew121410.mc.world16elevators.enums.ElevatorStatus;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
@AllArgsConstructor
public class FloorQueueObject {
    private int floorNumber;
    private ElevatorStatus elevatorStatus;
}