package com.andrew121410.mc.world16elevators;

import com.andrew121410.mc.world16elevators.commands.ElevatorCMD;
import com.andrew121410.mc.world16elevators.manager.ElevatorChunkSmartManager;
import com.andrew121410.mc.world16elevators.manager.ElevatorManager;
import com.andrew121410.mc.world16elevators.objects.*;
import com.andrew121410.mc.world16elevators.utils.OtherPlugins;
import com.andrew121410.mc.world16elevators.utils.SetListMap;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public final class World16Elevators extends JavaPlugin {

    static {
        ConfigurationSerialization.registerClass(SignObject.class, "SignObject");
        ConfigurationSerialization.registerClass(ElevatorMovement.class, "ElevatorMovement");
        ConfigurationSerialization.registerClass(ElevatorSettings.class, "ElevatorSettings");
        ConfigurationSerialization.registerClass(ElevatorSound.class, "ElevatorSound");
        ConfigurationSerialization.registerClass(FloorObject.class, "FloorObject");
        ConfigurationSerialization.registerClass(ElevatorObject.class, "ElevatorObject");
        ConfigurationSerialization.registerClass(ElevatorController.class, "ElevatorController");
    }

    private static World16Elevators instance;

    private ElevatorManager elevatorManager;

    private OtherPlugins otherPlugins;
    private SetListMap setListMap;

    private boolean chunkSmartManagement = false;

    @Override
    public void onEnable() {
        instance = this;
        this.otherPlugins = new OtherPlugins(this);
        this.setListMap = new SetListMap();

        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.reloadConfig();

        this.chunkSmartManagement = this.getConfig().getBoolean("ChunkSmartManagement");

        this.elevatorManager = new ElevatorManager(this);
        this.elevatorManager.loadAllElevators();

        if (chunkSmartManagement) {
            this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ElevatorChunkSmartManager(this), 200L, 200L);
        }

        regCommands();
    }

    @Override
    public void onDisable() {
        this.elevatorManager.saveAllElevators();
    }

    public void regCommands() {
        new ElevatorCMD(this);
    }

    public static World16Elevators getInstance() {
        return instance;
    }

    public OtherPlugins getOtherPlugins() {
        return otherPlugins;
    }

    public SetListMap getSetListMap() {
        return setListMap;
    }

    public ElevatorManager getElevatorManager() {
        return elevatorManager;
    }

    public boolean isChunkSmartManagement() {
        return chunkSmartManagement;
    }
}
