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

        // See if the bounding box is on the same x or z axis. If so this must be a small elevator.
        // So in this case we will expand the bounding box by 1 in all directions, for the teleporting bounding box.
        if (this.boundingBox.getMinX() == this.boundingBox.getMaxX() || this.boundingBox.getMinZ() == this.boundingBox.getMaxZ()) {
            this.teleportingBoundingBox = this.boundingBox.clone().expand(1);
        } else { // This must be a large elevator.
            // Expand the bounding box by -1 on the Y axis for minY and +1 on the Y axis for maxY
            this.teleportingBoundingBox = this.boundingBox.clone().expand(0.5, 1, 0.5);
        }
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

    /**
     * Validates that the elevator's bounding box position matches the actual elevator cabin location
     * by checking for non-air blocks within the bounding box at the expected position.
     *
     * @param world The world to check blocks in
     * @return true if the position appears to be correct (non-air blocks found), false if misaligned
     */
    public boolean validatePosition(org.bukkit.World world) {
        if (world == null || this.boundingBox == null || this.atDoor == null) {
            return false;
        }

        // Check for non-air blocks within the current bounding box position
        int checkY = this.atDoor.getBlockY();
        
        // Ensure bounding box has valid dimensions
        if (this.boundingBox.getVolume() <= 0) {
            return false;
        }
        
        // Count how many blocks we find vs how many we expect to check
        int blocksChecked = 0;
        int nonAirBlocksFound = 0;
        
        for (int x = (int) this.boundingBox.getMinX(); x <= this.boundingBox.getMaxX(); x++) {
            for (int z = (int) this.boundingBox.getMinZ(); z <= this.boundingBox.getMaxZ(); z++) {
                blocksChecked++;
                org.bukkit.block.Block block = world.getBlockAt(x, checkY, z);
                if (block.getType() != org.bukkit.Material.AIR) {
                    nonAirBlocksFound++;
                }
            }
        }
        
        // Consider position valid if we found at least some non-air blocks
        // (at least 10% of the bounding box should contain elevator structure)
        return nonAirBlocksFound > 0 && (blocksChecked == 0 || (double) nonAirBlocksFound / blocksChecked >= 0.1);
    }
}