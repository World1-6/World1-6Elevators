package com.andrew121410.mc.world16elevators.objects;


import com.andrew121410.mc.world16utils.chat.Translate;
import com.andrew121410.mc.world16utils.sign.SignUtils;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("SignObject")
public class SignObject implements ConfigurationSerializable {

    private Location location;

    public SignObject(Location location) {
        this.location = location;
    }

    public void doUpArrow() {
        Sign sign = SignUtils.isSign(location.getBlock());
        if (sign == null) return;

        String text = SignUtils.centerText("/\\", 16);
        String text1 = SignUtils.centerText("//\\\\", 16);
        sign.setLine(0, Translate.chat("&a&l" + text));
        sign.setLine(1, Translate.chat("&a&l" + text1));
        sign.update();
    }

    public void doDownArrow() {
        Sign sign = SignUtils.isSign(location.getBlock());
        if (sign == null) return;

        String text = SignUtils.centerText("\\\\//", 16);
        String text1 = SignUtils.centerText("\\/", 16);
        sign.setLine(2, Translate.chat("&c&l" + text));
        sign.setLine(3, Translate.chat("&c&l" + text1));
        sign.update();
    }

    public void clearSign() {
        Sign sign = SignUtils.isSign(location.getBlock());
        if (sign == null) return;

        sign.setLine(0, "");
        sign.setLine(1, "");
        sign.setLine(2, "");
        sign.setLine(3, "");
        sign.update();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("Location", location);
        return map;
    }

    public static SignObject deserialize(Map<String, Object> map) {
        return new SignObject((Location) map.get("Location"));
    }
}