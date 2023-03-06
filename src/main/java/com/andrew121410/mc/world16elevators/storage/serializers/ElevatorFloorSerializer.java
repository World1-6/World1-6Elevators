package com.andrew121410.mc.world16elevators.storage.serializers;

import com.andrew121410.mc.world16elevators.ElevatorFloor;
import com.andrew121410.mc.world16elevators.ElevatorSign;
import com.andrew121410.mc.world16utils.config.serializers.SerializerUtils;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.ConfigurationNode;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.serialize.SerializationException;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.serialize.TypeSerializer;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.util.List;

public class ElevatorFloorSerializer implements TypeSerializer<ElevatorFloor> {
    @Override
    public com.andrew121410.mc.world16elevators.ElevatorFloor deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.raw() == null) {
            return null;
        }

        Integer floor = SerializerUtils.nonVirtualNode(node, "Floor").getInt();
        String name = SerializerUtils.nonVirtualNode(node, "Name").getString();
        Location mainDoor = SerializerUtils.nonVirtualNode(node, "MainDoor").get(Location.class);
        List<Location> doorList = SerializerUtils.nonVirtualNode(node, "DoorList").getList(Location.class);
        List<ElevatorSign> signList = SerializerUtils.nonVirtualNode(node, "SignList").getList(ElevatorSign.class);
        String permission = SerializerUtils.nonVirtualNode(node, "Permission").getString();

        return new ElevatorFloor(floor, name, mainDoor, doorList, signList, permission);
    }

    @Override
    public void serialize(Type type, com.andrew121410.mc.world16elevators.ElevatorFloor obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.raw(null);
            return;
        }

        node.node("Floor").set(obj.getFloor());
        node.node("Name").set(obj.getName());
        node.node("MainDoor").set(obj.getBlockUnderMainDoor());
        node.node("DoorList").set(obj.getDoorList());
        node.node("SignList").set(obj.getSignList());
        node.node("Permission").set(obj.getPermission());
    }
}
