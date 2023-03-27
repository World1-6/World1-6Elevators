package com.andrew121410.mc.world16elevators;

import com.andrew121410.mc.world16elevators.commands.ElevatorCMD;
import com.andrew121410.mc.world16elevators.listeners.OnPlayerInteractEvent;
import com.andrew121410.mc.world16elevators.storage.ElevatorChunkSmartManager;
import com.andrew121410.mc.world16elevators.storage.ElevatorManager;
import com.andrew121410.mc.world16elevators.utils.OtherPlugins;
import com.andrew121410.mc.world16elevators.utils.MemoryHolder;
import com.andrew121410.mc.world16utils.updater.UpdateManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class World16Elevators extends JavaPlugin {

    private static World16Elevators instance;

    private ElevatorManager elevatorManager;

    private OtherPlugins otherPlugins;
    private MemoryHolder memoryHolder;

    private boolean chunkSmartManagement = false;

    @Override
    public void onEnable() {
        instance = this;
        this.otherPlugins = new OtherPlugins(this);
        this.memoryHolder = new MemoryHolder();

        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.reloadConfig();

        this.chunkSmartManagement = this.getConfig().getBoolean("ChunkSmartManagement");

        this.elevatorManager = new ElevatorManager(this);
        this.elevatorManager.loadAllElevatorControllers();

        if (chunkSmartManagement) {
            this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ElevatorChunkSmartManager(this), 200L, 200L);
        }

        registerEvents();
        registerCommands();

        UpdateManager.registerUpdater(this, new com.andrew121410.mc.world16elevators.Updater(this));
    }

    @Override
    public void onDisable() {
        this.elevatorManager.saveAllElevators();
    }

    public void registerEvents() {
        new OnPlayerInteractEvent(this);
    }

    public void registerCommands() {
        new ElevatorCMD(this);
    }

    public static World16Elevators getInstance() {
        return instance;
    }

    public OtherPlugins getOtherPlugins() {
        return otherPlugins;
    }

    public MemoryHolder getSetListMap() {
        return memoryHolder;
    }

    public ElevatorManager getElevatorManager() {
        return elevatorManager;
    }

    public boolean isChunkSmartManagement() {
        return chunkSmartManagement;
    }
}
