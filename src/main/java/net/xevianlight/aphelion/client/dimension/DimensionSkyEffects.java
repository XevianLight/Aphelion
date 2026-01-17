package net.xevianlight.aphelion.client.dimension;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.xevianlight.aphelion.Aphelion;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class DimensionSkyEffects extends DimensionSpecialEffects {

    private final ResourceLocation effectsId;

    boolean customSky;

    public DimensionSkyEffects(ResourceLocation effectsId) {
        super(192, false, SkyType.NORMAL, false, false);
        this.effectsId = effectsId;
    }

    private DimensionRenderer renderer() {
        return DimensionRendererCache.getOrDefault(effectsId);
    }

//    @Override
//    public boolean renderVoidFog() {
//        return true;
//    }


    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        return super.renderSky(level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        if (renderer().hasFog()) {
            return fogColor.multiply(
                    brightness * 0.94 + 0.06,
                    brightness * 0.94 + 0.06,
                    brightness * 0.91 + 0.09);
        }
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return renderer().hasThickFog();
    }

    @Override
    public @Nullable float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return new float[]{0,0,0,0};
    }

}

