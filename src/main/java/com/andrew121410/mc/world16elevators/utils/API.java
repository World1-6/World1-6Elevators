package com.andrew121410.mc.world16elevators.utils;

import com.andrew121410.mc.world16elevators.World16Elevators;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public class API {

    private World16Elevators plugin;

    public API(World16Elevators plugin) {
        this.plugin = plugin;
    }

    public Block getBlockPlayerIsLookingAt(Player player) {
        return player.getTargetBlock(null, 5);
    }

    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLong(String input) {
        try {
            Long.parseLong(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isBoolean(String boolean1) {
        return boolean1.equalsIgnoreCase("true") || boolean1.equalsIgnoreCase("false");
    }

    public Integer asIntOrDefault(String input, int default1) {
        try {
            Integer.parseInt(input);
            return Integer.valueOf(input);
        } catch (Exception e) {
            return default1;
        }
    }

    public Long asLongOrDefault(String input, long default1) {
        try {
            Long.parseLong(input);
            return Long.valueOf(input);
        } catch (Exception e) {
            return default1;
        }
    }

    public Double asDoubleOrDefault(String input, double default1) {
        try {
            Double.parseDouble(input);
            return Double.valueOf(input);
        } catch (Exception e) {
            return default1;
        }
    }

    public float asFloatOrDefault(String input, float default1) {
        try {
            Float.parseFloat(input);
            return Float.parseFloat(input);
        } catch (Exception e) {
            return default1;
        }
    }

    public boolean asBooleanOrDefault(String boolean1, boolean default1) {
        try {
            return Boolean.parseBoolean(boolean1);
        } catch (Exception e) {
            return default1;
        }
    }

    public static String getIndexFromStringArrayList(List<String> stringList, int index) {
        if (index >= 0 && index < stringList.size()) {
            return stringList.get(index);
        }
        return null;
    }
}
