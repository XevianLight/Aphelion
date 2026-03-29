package net.xevianlight.aphelion.planet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Orbit(
        int temp
) {
    public static final Codec<Orbit> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("temp").forGetter(Orbit::temp)
    ).apply(inst, Orbit::new));
}
