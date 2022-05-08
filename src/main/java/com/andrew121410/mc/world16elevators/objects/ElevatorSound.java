package com.andrew121410.mc.world16elevators.objects;

import lombok.*;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@SerializableAs("ElevatorSound")
public class ElevatorSound implements ConfigurationSerializable {
    private final Sound sound;
    private final float volume;
    private final float pitch;

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("Sound", this.sound.name());
        map.put("Volume", this.volume);
        map.put("Pitch", this.pitch);
        return map;
    }

    public static ElevatorSound deserialize(Map<String, Object> map) {
        double fakeVolume = (double) map.get("Volume");
        double fakePitch = (double) map.get("Pitch");
        return new ElevatorSound(Sound.valueOf((String) map.get("Sound")), (float) fakeVolume, (float) fakePitch);
    }
}