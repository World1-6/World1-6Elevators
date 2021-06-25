package com.andrew121410.mc.world16elevators.objects;


import com.andrew121410.mc.world16elevators.World16Elevators;
import com.andrew121410.mc.world16utils.blocks.BlockUtils;
import com.andrew121410.mc.world16utils.chat.Translate;
import com.andrew121410.mc.world16utils.sign.SignCache;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
@ToString
@Getter
@SerializableAs("SignObject")
public class SignObject implements ConfigurationSerializable {

    private Location location;
    private SignCache signCache;
    private BlockUtils blockUtils;

    public SignObject(Location location) {
        this.location = location;
        this.signCache = new SignCache();
        this.blockUtils = World16Elevators.getInstance().getOtherPlugins().getWorld16Utils().getClassWrappers().getBlockUtils();
    }

    public boolean doUpArrow() {
        Sign sign = blockUtils.isSign(location.getBlock());
        if (sign == null) return false;
        this.signCache.fromSign(sign);
        String text = BlockUtils.signCenterText("/\\");
        String text1 = BlockUtils.signCenterText("//\\\\");
        sign.setLine(0, Translate.chat("&a&l" + text));
        sign.setLine(1, Translate.chat("&a&l" + text1));
        sign.setLine(2, "");
        sign.setLine(3, "");
        sign.update();
        return true;
    }

    public boolean doDownArrow() {
        Sign sign = blockUtils.isSign(location.getBlock());
        if (sign == null) return false;
        this.signCache.fromSign(sign);
        String text = BlockUtils.signCenterText("\\\\//");
        String text1 = BlockUtils.signCenterText("\\/");
        sign.setLine(0, "");
        sign.setLine(1, "");
        sign.setLine(2, Translate.chat("&c&l" + text));
        sign.setLine(3, Translate.chat("&c&l" + text1));
        sign.update();
        return true;
    }

    public boolean revert() {
        Sign sign = blockUtils.isSign(location.getBlock());
        if (sign == null) return false;
        this.signCache.updateFancy(sign);
        return true;
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