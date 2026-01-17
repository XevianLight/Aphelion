package net.xevianlight.aphelion.client.dimension;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.xevianlight.aphelion.Aphelion;
import org.joml.Matrix4f;

public class SpaceSkyEffects extends DimensionSpecialEffects {

    public SpaceSkyEffects() {
        super(192, false, SkyType.NORMAL, false, false);
    }



    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        ResourceLocation id = orbitForPos(net.minecraft.client.Minecraft.getInstance()
                .gameRenderer.getMainCamera().getPosition());
//        Aphelion.LOGGER.info("Loaded dimension_renderers: {}", DimensionRendererCache.getOrDefault(id).toString());
        // Return true, meaning we are rendering the sky ourselves. Vanilla will not draw its sky.
        if (DimensionRendererCache.getOrDefault(id).customSky())
            return true;
        return super.renderSky(level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        ResourceLocation id = orbitForPos(net.minecraft.client.Minecraft.getInstance()
                .gameRenderer.getMainCamera().getPosition());
        if (DimensionRendererCache.getOrDefault(id).hasFog()) {
            return fogColor.multiply(
                    brightness * 0.94 + 0.06,
                    brightness * 0.94 + 0.06,
                    brightness * 0.91 + 0.09);
        }
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int i, int i1) {
        ResourceLocation id = orbitForPos(net.minecraft.client.Minecraft.getInstance()
                .gameRenderer.getMainCamera().getPosition());

        return DimensionRendererCache.getOrDefault(id).hasThickFog();
    }

    public static ResourceLocation orbitForPos(Vec3 pos) {
        double r = Math.sqrt(pos.x * pos.x + pos.z * pos.z);

        if (r < 100) return ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "orbit/earth");
        if (r < 200) return ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "orbit/mars");
        if (r < 300) return ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "orbit/venus");

        return ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "orbit/default");
    }
}

