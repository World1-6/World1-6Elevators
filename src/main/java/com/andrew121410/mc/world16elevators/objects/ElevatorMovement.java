package com.andrew121410.mc.world16elevators.objects;

import lombok.*;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@AllArgsConstructor
@SerializableAs("ElevatorMovement")
public class ElevatorMovement implements ConfigurationSerializable {

    private Integer floor;

    private Location atDoor;
    private BoundingBox boundingBox;

    public void moveUP() {
        this.atDoor.add(0, 1, 0);
        this.boundingBox.shift(0, 1, 0);
    }

    public void moveDOWN() {
        this.atDoor.subtract(0, 1, 0);
        this.boundingBox.shift(0, -1, 0);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("Floor", this.floor);
        map.put("AtDoor", this.atDoor);
        map.put("BoundingBox", this.boundingBox);
        return map;
    }

    public static ElevatorMovement deserialize(Map<String, Object> map) {
        return new ElevatorMovement((Integer) map.get("Floor"),
                (Location) map.get("AtDoor"),
                (BoundingBox) map.get("BoundingBox"));
    }
}