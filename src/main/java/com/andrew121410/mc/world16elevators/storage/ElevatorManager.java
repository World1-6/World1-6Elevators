package com.andrew121410.mc.world16elevators.storage;

import com.andrew121410.mc.world16elevators.*;
import com.andrew121410.mc.world16elevators.storage.serializers.*;
import com.andrew121410.mc.world16utils.config.World16ConfigurateManager;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.CommentedConfigurationNode;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.serialize.TypeSerializerCollection;
import com.andrew121410.mc.world16utils.utils.spongepowered.configurate.yaml.YamlConfigurationLoader;
import lombok.SneakyThrows;
import org.bukkit.Location;

import java.util.Map;

public class ElevatorManager {

    private final Map<Location, String> chunksToControllerNameMap;
    private final Map<String, ElevatorController> elevatorControllerMap;

    private final World16Elevators plugin;
    private final YamlConfigurationLoader elevatorsYml;

    public ElevatorManager(World16Elevators plugin) {
        this.plugin = plugin;
        this.chunksToControllerNameMap = this.plugin.getSetListMap().getChunksToControllerNameMap();
        this.elevatorControllerMap = this.plugin.getSetListMap().getElevatorControllerMap();

        World16ConfigurateManager world16ConfigurateManager = new World16ConfigurateManager(this.plugin);
        world16ConfigurateManager.registerTypeSerializerCollection(getOurSerializers());
        this.elevatorsYml = world16ConfigurateManager.getYamlConfigurationLoader("elevators.yml");
    }

    private TypeSerializerCollection getOurSerializers() {
        TypeSerializerCollection.Builder ourSerializers = TypeSerializerCollection.builder();

        ourSerializers.registerExact(ElevatorController.class, new ElevatorControllerSerializer());
        ourSerializers.registerExact(ElevatorFloor.class, new ElevatorFloorSerializer());
        ourSerializers.registerExact(ElevatorMovement.class, new ElevatorMovementSerializer());
        ourSerializers.registerExact(Elevator.class, new ElevatorSerializer());
        ourSerializers.registerExact(ElevatorSign.class, new ElevatorSignSerializer());

        return ourSerializers.build();
    }

    @SneakyThrows
    public void loadAllElevatorControllers() {
        CommentedConfigurationNode node = this.elevatorsYml.load().node("ElevatorControllers");

        for (Map.Entry<Object, CommentedConfigurationNode> objectCommentedConfigurationNodeEntry : node.childrenMap().entrySet()) {
            String key = (String) objectCommentedConfigurationNodeEntry.getKey();
            ElevatorController elevatorController = objectCommentedConfigurationNodeEntry.getValue().get(ElevatorController.class);

            if (this.plugin.isChunkSmartManagement())
                this.chunksToControllerNameMap.put(elevatorController.getMainChunk(), key);
            else this.elevatorControllerMap.put(key, elevatorController);
        }
    }

    @SneakyThrows
    public void saveAllElevators() {
        CommentedConfigurationNode node = this.elevatorsYml.load();

        for (Map.Entry<String, ElevatorController> mapEntry : this.elevatorControllerMap.entrySet()) {
            String key = mapEntry.getKey();
            ElevatorController elevatorController = mapEntry.getValue();
            node.node("ElevatorControllers", key).set(elevatorController);
            this.elevatorsYml.save(node);
        }
    }

    @SneakyThrows
    public ElevatorController loadElevatorController(String key) {
        CommentedConfigurationNode node = this.elevatorsYml.load().node("ElevatorControllers");

        ElevatorController elevatorController = node.node(key).get(ElevatorController.class);
        this.elevatorControllerMap.put(key, elevatorController);

        return elevatorController;
    }

    @SneakyThrows
    public void saveAndUnloadElevatorController(ElevatorController elevatorController) {
        CommentedConfigurationNode node = this.elevatorsYml.load().node("ElevatorControllers");

        node.node(elevatorController.getControllerName()).set(elevatorController);
        this.elevatorsYml.save(node);

        this.elevatorControllerMap.remove(elevatorController.getControllerName());
    }

    @SneakyThrows
    public void deleteElevatorController(String name) {
        ElevatorController elevatorController = this.elevatorControllerMap.get(name.toLowerCase());
        if (elevatorController.getMainChunk() != null) {
            this.chunksToControllerNameMap.remove(elevatorController.getMainChunk());
        }
        this.elevatorControllerMap.remove(name.toLowerCase());
        CommentedConfigurationNode node = this.elevatorsYml.load().node("ElevatorControllers");
        node.removeChild(name.toLowerCase());
        this.elevatorsYml.save(node);
    }

    @SneakyThrows
    public void deleteElevator(String elevatorControllerName, String elevatorName) {
        ElevatorController elevatorController = this.elevatorControllerMap.get(elevatorControllerName);
        if (elevatorController == null) return;
        elevatorController.getElevatorsMap().remove(elevatorName);
        CommentedConfigurationNode node = this.elevatorsYml.load().node("ElevatorControllers", elevatorControllerName.toLowerCase(), "ElevatorMap");
        node.removeChild(elevatorName);
        this.elevatorsYml.save(node);
    }
}