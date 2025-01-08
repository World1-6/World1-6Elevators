package com.andrew121410.mc.world16elevators;

import com.andrew121410.mc.world16utils.dependencies.spongepowered.configurate.objectmapping.ConfigSerializable;
import com.andrew121410.mc.world16utils.dependencies.spongepowered.configurate.objectmapping.meta.Setting;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Sound;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@ConfigSerializable
public class ElevatorSound {

    @Setting(value = "Sound")
    private final Sound sound;
    @Setting(value = "Volume")
    private final float volume;
    @Setting(value = "Pitch")
    private final float pitch;

    public ElevatorSound() {
        this.sound = Sound.BLOCK_NOTE_BLOCK_PLING;
        this.volume = 1.0F;
        this.pitch = 1.0F;
    }
}