package net.xevianlight.aphelion.planet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class AphelionPlanetJSONLoader extends SimpleJsonResourceReloadListener {

    public AphelionPlanetJSONLoader() {
        super(Constants.GSON, "planet");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {

        Map<ResourceLocation, Planet> planets = new HashMap<>();
        object.forEach((key, value) -> {
            JsonObject json = GsonHelper.convertToJsonObject(value, "planet");
            Planet planet = Planet.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();

            // IMPORTANT: use the *resource id* of the json as the lookup key
            // so "effects": "aphelion:space" maps to space.json automatically.
            planets.put(key, planet);
        });

        Aphelion.LOGGER.info("Loaded planets " + planets);

        PlanetCache.registerPlanets(planets);
    }
}
