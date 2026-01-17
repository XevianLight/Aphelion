package net.xevianlight.aphelion.client.dimension;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public final class DimensionRendererCache {

    public static final Map<ResourceLocation, DimensionRenderer> RENDERERS = new HashMap<>();

    public static void registerPlanetRenderers(Map<ResourceLocation, DimensionRenderer> renderers) {
        RENDERERS.clear();
        RENDERERS.putAll(renderers);

    }

    public static DimensionRenderer getOrDefault(ResourceLocation id) {
        return RENDERERS.getOrDefault(id, null);
    }
}
