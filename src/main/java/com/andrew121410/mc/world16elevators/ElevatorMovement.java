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
    private BoundingBox boundingBox; // This is the bounding box of the blocks we have to move, up and down.
    private BoundingBox teleportingBoundingBox; // This bounding box is used for teleporting the player up and down.

    public ElevatorMovement(Integer floor, Location atDoor, BoundingBox boundingBox, BoundingBox teleportingBoundingBox) {
        this.floor = floor;
        this.atDoor = ElevatorFloor.ifIronDoorThenGetBlockUnderTheDoorIfNotThanReturn(atDoor).getLocation();
        this.boundingBox = boundingBox;
        this.teleportingBoundingBox = teleportingBoundingBox;
    }

    public ElevatorMovement(Integer floor, Location atDoor, BoundingBox boundingBox) {
        this.floor = floor;
        this.atDoor = ElevatorFloor.ifIronDoorThenGetBlockUnderTheDoorIfNotThanReturn(atDoor).getLocation();
        this.boundingBox = boundingBox;
    }

    /**
     * Shift the bounding box and the atDoor location by the amount.
     *
     * @param amount The amount to shift by. Positive values will shift up, negative values will shift down.
     */
    public void shiftY(int amount) {
        this.atDoor.add(0, amount, 0);
        this.boundingBox.shift(0, amount, 0);
        this.teleportingBoundingBox.shift(0, amount, 0);
    }
}