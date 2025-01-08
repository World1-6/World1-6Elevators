package com.andrew121410.mc.world16elevators.storage.serializers;

import com.andrew121410.mc.world16elevators.Elevator;
import com.andrew121410.mc.world16elevators.ElevatorController;
import com.andrew121410.mc.world16elevators.World16Elevators;
import com.andrew121410.mc.world16utils.config.serializers.SerializerUtils;
import com.andrew121410.mc.world16utils.dependencies.spongepowered.configurate.ConfigurationNode;
import com.andrew121410.mc.world16utils.dependencies.spongepowered.configurate.serialize.SerializationException;
import com.andrew121410.mc.world16utils.dependencies.spongepowered.configurate.serialize.TypeSerializer;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Type;
import java.util.Map;

public class ElevatorControllerSerializer implements TypeSerializer<ElevatorController> {
    @Override
    public ElevatorController deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.raw() == null) {
            return null;
        }

        String controllerName = SerializerUtils.nonVirtualNode(node, "ControllerName").getString();
        Location mainChunk = SerializerUtils.nonVirtualNode(node, "MainChunk").get(Location.class);

        TypeToken<Map<String, Elevator>> elevatorMapTypeToken = new TypeToken<>() {
        };
        Map<String, Elevator> elevatorsMap = SerializerUtils.nonVirtualNode(node, "ElevatorMap").get(elevatorMapTypeToken);

        return new ElevatorController(World16Elevators.getInstance(), controllerName, mainChunk, elevatorsMap);
    }

    @Override
    public void serialize(Type type, @Nullable ElevatorController obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.raw(null);
            return;
        }

        node.node("ControllerName").set(obj.getControllerName());
        node.node("MainChunk").set(obj.getMainChunk());

        TypeToken<Map<String, Elevator>> elevatorMapTypeToken = new TypeToken<>() {
        };
        node.node("ElevatorMap").set(elevatorMapTypeToken, obj.getElevatorsMap());
    }
}
