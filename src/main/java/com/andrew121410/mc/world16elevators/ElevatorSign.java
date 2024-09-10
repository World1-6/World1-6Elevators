package com.andrew121410.mc.world16elevators;


import com.andrew121410.mc.world16utils.blocks.UniversalBlockUtils;
import com.andrew121410.mc.world16utils.chat.Translate;
import com.andrew121410.mc.world16utils.sign.SignCache;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;

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
        SignSide side = sign.getSide(Side.FRONT);
        String text = UniversalBlockUtils.signCenterText("/\\");
        String text1 = UniversalBlockUtils.signCenterText("//\\\\");
        side.line(0, Translate.colorc("&a&l" + text));
        side.line(1, Translate.colorc("&a&l" + text1));
        side.line(2, Component.empty());
        side.line(3, Component.empty());
        sign.update();
        return true;
    }

    public boolean doDownArrow() {
        Sign sign = UniversalBlockUtils.isSign(location.getBlock());
        if (sign == null) return false;
        this.signCache.fromSign(sign);
        SignSide side = sign.getSide(Side.FRONT);
        String text = UniversalBlockUtils.signCenterText("\\\\//");
        String text1 = UniversalBlockUtils.signCenterText("\\/");
        side.line(0, Component.empty());
        side.line(1, Component.empty());
        side.line(2, Translate.colorc("&c&l" + text));
        side.line(3, Translate.colorc("&c&l" + text1));
        sign.update();
        return true;
    }

    public boolean revert() {
        Sign sign = UniversalBlockUtils.isSign(location.getBlock());
        if (sign == null) return false;
        this.signCache.update(sign);
        return true;
    }
}