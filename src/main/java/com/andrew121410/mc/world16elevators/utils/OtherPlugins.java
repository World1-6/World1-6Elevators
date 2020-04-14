package com.andrew121410.mc.world16elevators.utils;

import com.andrew121410.mc.world16elevators.Main;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.plugin.Plugin;

public class OtherPlugins {

    private Main plugin;

    //Plugins
    //WorldEdit
    private WorldEditPlugin worldEditPlugin;

    public OtherPlugins(Main plugin) {
        this.plugin = plugin;

        setupWorldEditPlugin();
    }

    private void setupWorldEditPlugin() {
        Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("WorldEdit");

        if (plugin instanceof WorldEditPlugin) {
            this.worldEditPlugin = (WorldEditPlugin) plugin;
        }
    }

    public WorldEditPlugin getWorldEditPlugin() {
        return worldEditPlugin;
    }
}
