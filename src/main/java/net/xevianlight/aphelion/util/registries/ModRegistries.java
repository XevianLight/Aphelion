package net.xevianlight.aphelion.util.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.planet.Orbit;
import net.xevianlight.aphelion.planet.Planet;
import net.xevianlight.aphelion.planet.StarSystem;

public class ModRegistries {
    public static final ResourceKey<Registry<StarSystem>> STAR_SYSTEM = createRegistryKey("star_system");
    public static final ResourceKey<Registry<Planet>> PLANET = createRegistryKey("planet");
    public static final ResourceKey<Registry<Orbit>> ORBIT = createRegistryKey("orbit");

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(Aphelion.id(name));
    }
}
