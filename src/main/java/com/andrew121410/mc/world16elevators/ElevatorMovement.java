package com.andrew121410.mc.world16elevators;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

@EqualsAndHashCode
@ToString
@Getter
@Setter
public class ElevatorMovement {

    private Integer floor;

    private Location atDoor;
    private BoundingBox boundingBox;

    public ElevatorMovement(Integer floor, Location atDoor, BoundingBox boundingBox) {
        this.floor = floor;
        this.atDoor = ElevatorFloor.ifIronDoorThenGetBlockUnderTheDoorIfNotThanReturn(atDoor).getLocation();
        this.boundingBox = boundingBox;
    }
}