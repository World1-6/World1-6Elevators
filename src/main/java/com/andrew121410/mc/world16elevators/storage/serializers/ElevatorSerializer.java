package com.andrew121410.mc.world16elevators.storage.serializers;

import com.andrew121410.mc.world16elevators.*;
import com.andrew121410.mc.world16utils.config.serializers.SerializerUtils;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.ConfigurationNode;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.serialize.SerializationException;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Type;
import java.util.Map;

public class ElevatorSerializer implements TypeSerializer<Elevator> {
    @Override
    public Elevator deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.raw() == null) {
            return null;
        }

        String name = SerializerUtils.nonVirtualNode(node, "Name").getString();
        String world = SerializerUtils.nonVirtualNode(node, "World").getString();
        ElevatorMovement elevatorMovement = SerializerUtils.nonVirtualNode(node, "Shaft").get(ElevatorMovement.class);
        ElevatorSettings elevatorSettings = SerializerUtils.nonVirtualNode(node, "Settings").get(ElevatorSettings.class);
        Map<Integer, ElevatorFloor> elevatorFloorMap = SerializerUtils.nonVirtualNode(node, "FloorMap").get(Map.class);

        return new Elevator(World16Elevators.getInstance(), name, world, elevatorMovement, elevatorSettings, elevatorFloorMap);
    }

    @Override
    public void serialize(Type type, @Nullable Elevator obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.raw(null);
            return;
        }

        node.node("Name").set(obj.getElevatorName());
        node.node("World").set(obj.getWorld());
        node.node("Shaft").set(obj.getElevatorMovement());
        node.node("Settings").set(obj.getElevatorSettings());
        node.node("FloorMap").set(obj.getFloorsMap());
    }
}
