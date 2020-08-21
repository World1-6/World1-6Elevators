package com.andrew121410.mc.world16elevators.utils;

import com.andrew121410.mc.world16elevators.Main;
import com.andrew121410.mc.world16utils.World16Utils;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.plugin.Plugin;

public class OtherPlugins {

    private Main plugin;

    //Plugins
    private World16Utils world16Utils;
    //WorldEdit
    private WorldEditPlugin worldEditPlugin;

    public OtherPlugins(Main plugin) {
        this.plugin = plugin;

        setupWorld16Utils();
        setupWorldEditPlugin();
    }

    private void setupWorld16Utils() {
        Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("World1-6Utils");

        if (plugin instanceof World16Utils) {
            this.world16Utils = (World16Utils) plugin;
        }
    }

    private void setupWorldEditPlugin() {
        Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("WorldEdit");

        if (plugin instanceof WorldEditPlugin) {
            this.worldEditPlugin = (WorldEditPlugin) plugin;
        }
    }

    public World16Utils getWorld16Utils() {
        return world16Utils;
    }

    public WorldEditPlugin getWorldEditPlugin() {
        return worldEditPlugin;
    }
}
