package net.xevianlight.aphelion.planet;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.util.registries.ModRegistries;

import java.util.HashMap;
import java.util.Map;

public final class PlanetCache {

    public static final Map<ResourceLocation, Planet> PLANETS = new HashMap<>();
    public static final Map<ResourceKey<Level>, ResourceLocation> PLANET_BY_DIMENSION = new HashMap<>();

    public static final Planet DEFAULT = new Planet(
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("overworld")),
            1,
            ResourceKey.create(ModRegistries.STAR_SYSTEM, Aphelion.id("sol")),
            true,
            1
    );

    public static void registerPlanets(Map<ResourceLocation, Planet> planets) {
        PLANETS.clear();
        PLANET_BY_DIMENSION.clear();

        PLANETS.putAll(planets);

        planets.forEach((planetId, planet) -> {
            var dim = planet.dimension();
            var prev = PLANET_BY_DIMENSION.put(dim, planetId);
            if (prev != null) {
                Aphelion.LOGGER.warn(
                        "Dimension {} is claimed by multiple planets: {} and {}. Keeping latest: {}",
                        dim.location(), prev, planetId, planetId
                );
            }
        });

        Aphelion.LOGGER.info("Loaded {} planets; {} dimension mappings",
                PLANETS.size(), PLANET_BY_DIMENSION.size());
    }

    public static Planet getOrDefault(ResourceLocation id) {
        return PLANETS.getOrDefault(id, DEFAULT);
    }

    public static Planet getByDimensionOrNull(ResourceKey<Level> dimension) {
        ResourceLocation planetId = PLANET_BY_DIMENSION.get(dimension);
        return planetId == null ? null : PLANETS.get(planetId);
    }
}
