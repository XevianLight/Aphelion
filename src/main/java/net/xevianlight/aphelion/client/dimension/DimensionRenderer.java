package net.xevianlight.aphelion.client.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Optional;

public record DimensionRenderer(
        ResourceKey<Level> dimension,
        boolean customClouds,
        boolean customSky,
        boolean customWeather,
        boolean hasThickFog,
        boolean hasFog,
        int sunriseColor,
        int sunriseAngle,
        boolean renderInRain,
        boolean renderVoidFog,
        double horizonHeight,
        float clearColorScale
) {
    public static final Codec<DimensionRenderer> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(DimensionRenderer::dimension),
        Codec.BOOL.fieldOf("custom_clouds").forGetter(DimensionRenderer::customClouds),
        Codec.BOOL.fieldOf("custom_sky").forGetter(DimensionRenderer::customSky),
        Codec.BOOL.fieldOf("custom_weather").forGetter(DimensionRenderer::customWeather),
        Codec.BOOL.fieldOf("has_thick_fog").forGetter(DimensionRenderer::hasThickFog),
        Codec.BOOL.fieldOf("has_fog").forGetter(DimensionRenderer::hasFog),
        Codec.INT.fieldOf("sunrise_color").forGetter(DimensionRenderer::sunriseColor),
        Codec.INT.fieldOf("sunrise_angle").forGetter(DimensionRenderer::sunriseAngle),
        Codec.BOOL.fieldOf("render_in_rain").forGetter(DimensionRenderer::renderInRain),
        Codec.BOOL.fieldOf("render_void_fog").forGetter(DimensionRenderer::renderVoidFog),
        Codec.DOUBLE.fieldOf("horizon_height").forGetter(DimensionRenderer::horizonHeight),
        Codec.FLOAT.fieldOf("clear_color_scale").forGetter(DimensionRenderer::clearColorScale)
    ).apply(inst, DimensionRenderer::new));
}
