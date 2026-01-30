package net.xevianlight.aphelion.client.dimension;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.client.PartitionClientState;
import net.xevianlight.aphelion.util.SpacePartitionHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class SpaceSkyEffects extends DimensionSpecialEffects {

    private final ResourceLocation effectsId;

    public SpaceSkyEffects(@Nullable ResourceLocation effectsId) {
        super(192, false, SkyType.NORMAL, false, false);
        this.effectsId = effectsId;
    }

    public static ResourceLocation resolvedId(ResourceLocation effectsId, Camera camera) {
        if (effectsId == null) {
            return ResourceLocation.withDefaultNamespace("overworld");
        }
        if (effectsId.equals(Aphelion.id("space"))) {
            return orbitForPos(camera.getPosition()); // or inline this logic
        }
        return effectsId;
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

        ResourceLocation id = resolvedId(effectsId ,net.minecraft.client.Minecraft.getInstance()
                .gameRenderer.getMainCamera());

        return DimensionRendererCache.getOrDefault(id).hasThickFog();
    }

    public static ResourceLocation orbitForPos(Vec3 pos) {

        int x = SpacePartitionHelper.get(pos.x);
        int z = SpacePartitionHelper.get(pos.z);

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "orbit/default");

//        int px = PartitionClientState.pxOr(0);
//        int py = PartitionClientState.pyOr(0);
        var data = ResourceLocation.parse(PartitionClientState.idOrUnknown());

//        var partitionData = SpacePartitionSavedData.get(serverLevel).getOrbitForPartition((int)   x, (int) z);
        if (data != null) return data;

        return ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "orbit/default");
    }
}

