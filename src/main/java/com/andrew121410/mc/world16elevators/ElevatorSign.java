package com.andrew121410.mc.world16elevators;


import com.andrew121410.mc.world16utils.blocks.UniversalBlockUtils;
import com.andrew121410.mc.world16utils.chat.Translate;
import com.andrew121410.mc.world16utils.sign.SignCache;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.block.Sign;

@EqualsAndHashCode
@ToString
@Getter
public class ElevatorSign {

    private Location location;
    private SignCache signCache;

    public ElevatorSign(Location location) {
        this.location = location;
        this.signCache = new SignCache();
    }

    public boolean doUpArrow() {
        Sign sign = UniversalBlockUtils.isSign(location.getBlock());
        if (sign == null) return false;
        this.signCache.fromSign(sign);
        String text = UniversalBlockUtils.signCenterText("/\\");
        String text1 = UniversalBlockUtils.signCenterText("//\\\\");
        sign.setLine(0, Translate.chat("&a&l" + text));
        sign.setLine(1, Translate.chat("&a&l" + text1));
        sign.setLine(2, "");
        sign.setLine(3, "");
        sign.update();
        return true;
    }

    public boolean doDownArrow() {
        Sign sign = UniversalBlockUtils.isSign(location.getBlock());
        if (sign == null) return false;
        this.signCache.fromSign(sign);
        String text = UniversalBlockUtils.signCenterText("\\\\//");
        String text1 = UniversalBlockUtils.signCenterText("\\/");
        sign.setLine(0, "");
        sign.setLine(1, "");
        sign.setLine(2, Translate.chat("&c&l" + text));
        sign.setLine(3, Translate.chat("&c&l" + text1));
        sign.update();
        return true;
    }

    public boolean revert() {
        Sign sign = UniversalBlockUtils.isSign(location.getBlock());
        if (sign == null) return false;
        this.signCache.updateFancy(sign);
        return true;
    }
}