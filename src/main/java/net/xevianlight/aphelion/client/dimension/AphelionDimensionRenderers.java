package net.xevianlight.aphelion.client.dimension;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class AphelionDimensionRenderers extends SimpleJsonResourceReloadListener {

    public AphelionDimensionRenderers() {
        super(Constants.GSON, "dimension_renderers");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {

        Map<ResourceLocation, DimensionRenderer> renderers = new HashMap<>();
        object.forEach((key, value) -> {
            JsonObject json = GsonHelper.convertToJsonObject(value, "dimension_renderer");
            DimensionRenderer renderer = DimensionRenderer.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();

            // IMPORTANT: use the *resource id* of the json as the lookup key
            // so "effects": "aphelion:space" maps to space.json automatically.
            renderers.put(key, renderer);
        });

        DimensionRendererCache.registerPlanetRenderers(renderers);
    }
}
