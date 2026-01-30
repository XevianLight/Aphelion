package net.xevianlight.aphelion.planet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.xevianlight.aphelion.util.registries.ModRegistries;

public record Planet(
        ResourceKey<Level> dimension,
        double orbitDistance,
        ResourceKey<StarSystem> system,
        boolean oxygen,
        double gravity
) {
    public static final Codec<Planet> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Planet::dimension),
            Codec.DOUBLE.fieldOf("orbit_distance").forGetter(Planet::orbitDistance),
            ResourceKey.codec(ModRegistries.STAR_SYSTEM).fieldOf("star_system").forGetter(Planet::system),
            Codec.BOOL.fieldOf("oxygen").forGetter(Planet::oxygen),
            Codec.DOUBLE.fieldOf("gravity").forGetter(Planet::gravity)

    ).apply(inst, Planet::new));
}
