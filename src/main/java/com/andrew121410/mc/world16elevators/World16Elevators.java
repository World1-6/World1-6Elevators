package com.andrew121410.mc.world16elevators;

import com.andrew121410.mc.world16elevators.commands.ElevatorCMD;
import com.andrew121410.mc.world16elevators.listeners.OnPlayerInteractEvent;
import com.andrew121410.mc.world16elevators.storage.ElevatorChunkSmartManager;
import com.andrew121410.mc.world16elevators.storage.ElevatorManager;
import com.andrew121410.mc.world16elevators.utils.MemoryHolder;
import com.andrew121410.mc.world16elevators.utils.OtherPlugins;
import com.andrew121410.mc.world16utils.updater.UpdateManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class World16Elevators extends JavaPlugin {

    private static World16Elevators instance;

    private ElevatorManager elevatorManager;

    private OtherPlugins otherPlugins;
    private MemoryHolder memoryHolder;

    // Global Config
    private boolean chunkSmartManagement = false;
    private int maxSizeOfElevator;
    private List<String> elevatorCreationWhitelist;

    @Override
    public void onEnable() {
        instance = this;
        this.otherPlugins = new OtherPlugins(this);
        this.memoryHolder = new MemoryHolder();

        handleConfig();

        this.elevatorManager = new ElevatorManager(this);
        this.elevatorManager.loadAllElevatorControllers();

        if (chunkSmartManagement) {
            this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ElevatorChunkSmartManager(this), 200L, 200L);
        }

        registerEvents();
        registerCommands();

        UpdateManager.registerUpdater(this, new com.andrew121410.mc.world16elevators.Updater(this));
    }

    private void handleConfig() {
        saveDefaultConfig(); // Saves the default config.yml with comments if config.yml doesn't exist
        this.getConfig().options().copyDefaults(true); // Ensures all defaults are in place
        this.saveConfig(); // Saves the config again
        this.reloadConfig(); // Reloads the config, including any new defaults

        this.chunkSmartManagement = this.getConfig().getBoolean("ChunkSmartManagement");
        this.maxSizeOfElevator = this.getConfig().getInt("maxSizeOfElevator");

        // Elevator Creation Whitelist
        ConfigurationSection whitelistSection = this.getConfig().getConfigurationSection("whitelist");
        if (whitelistSection != null) {
            boolean isEnable = whitelistSection.getBoolean("enabled");
            if (isEnable) {
                this.elevatorCreationWhitelist = whitelistSection.getStringList("players");
            }else {
                this.elevatorCreationWhitelist = null;
            }
        }
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

    public MemoryHolder getMemoryHolder() {
        return memoryHolder;
    }

    public ElevatorManager getElevatorManager() {
        return elevatorManager;
    }

    public boolean isChunkSmartManagement() {
        return chunkSmartManagement;
    }

    public int getMaxSizeOfElevator() {
        return maxSizeOfElevator;
    }

    public List<String> getElevatorCreationWhitelist() {
        return elevatorCreationWhitelist;
    }
}
