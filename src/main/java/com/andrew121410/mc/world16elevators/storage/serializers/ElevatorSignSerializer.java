package com.andrew121410.mc.world16elevators.storage.serializers;

import com.andrew121410.mc.world16elevators.ElevatorSign;
import com.andrew121410.mc.world16utils.config.serializers.SerializerUtils;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.ConfigurationNode;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.serialize.SerializationException;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.serialize.TypeSerializer;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Type;

public class ElevatorSignSerializer implements TypeSerializer<ElevatorSign> {
    @Override
    public ElevatorSign deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.raw() == null) {
            return null;
        }

        Location location = SerializerUtils.nonVirtualNode(node, "Location").get(Location.class);

        return new ElevatorSign(location);
    }

    @Override
    public void serialize(Type type, @Nullable ElevatorSign obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.raw(null);
            return;
        }

        node.node("Location").set(obj.getLocation());
    }
}
