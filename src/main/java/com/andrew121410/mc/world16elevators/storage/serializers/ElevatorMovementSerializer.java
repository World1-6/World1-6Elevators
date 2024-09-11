package com.andrew121410.mc.world16elevators.storage.serializers;

import com.andrew121410.mc.world16elevators.ElevatorMovement;
import com.andrew121410.mc.world16utils.config.serializers.SerializerUtils;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.ConfigurationNode;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.serialize.SerializationException;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.serialize.TypeSerializer;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Type;

public class ElevatorMovementSerializer implements TypeSerializer<ElevatorMovement> {
    @Override
    public ElevatorMovement deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.raw() == null) {
            return null;
        }

        Integer floor = node.node("Floor").get(Integer.class);
        Location location = SerializerUtils.nonVirtualNode(node, "AtDoor").get(Location.class);
        BoundingBox boundingBox = SerializerUtils.nonVirtualNode(node, "BoundingBox").get(BoundingBox.class);

        return new ElevatorMovement(floor, location, boundingBox);
    }

    @Override
    public void serialize(Type type, @Nullable ElevatorMovement obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.raw(null);
            return;
        }

        node.node("Floor").set(obj.getFloor());
        node.node("AtDoor").set(obj.getAtDoor());
        node.node("BoundingBox").set(obj.getBoundingBox());
    }
}
