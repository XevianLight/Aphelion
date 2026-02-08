package net.xevianlight.aphelion.block.entity.custom.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.xevianlight.aphelion.block.entity.custom.RocketAssemblerBlockEntity;
import org.jetbrains.annotations.NotNull;

public class RocketAssemblerBlockEntityRenderer implements BlockEntityRenderer<RocketAssemblerBlockEntity> {

    public RocketAssemblerBlockEntityRenderer (BlockEntityRendererProvider.Context context) {

    }

    @Override
    public AABB getRenderBoundingBox(RocketAssemblerBlockEntity blockEntity) {
        // If we don't know bounds yet, fall back to default BE culling.
        RocketAssemblerBlockEntity.PadInfo pad = blockEntity.getPadBounds();
        if (pad == null) {
            return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
        }

        BlockPos min = pad.min();
        BlockPos max = pad.max();

        // Expand slightly to avoid edge precision culling
        return new AABB(
                min.getX(), min.getY(), min.getZ(),
                max.getX() + 1, max.getY() + 1, max.getZ() + 1
        ).inflate(0.5);
    }

    @Override
    public void render(@NotNull RocketAssemblerBlockEntity be, float v, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i, int i1) {
//        if (!Minecraft.getInstance().gui.getDebugOverlay().showDebugScreen()) return;

        if (be.getPadBounds() == null) return;
        BlockPos min = be.getPadBounds().min();
        BlockPos max = be.getPadBounds().max();

        AABB box = new AABB(
                min.getX(), min.getY(), min.getZ(),
                max.getX() + 1, max.getY() + 1, max.getZ() + 1
        );

        poseStack.pushPose();
        poseStack.translate(-be.getBlockPos().getX(), -be.getBlockPos().getY(), -be.getBlockPos().getZ());

        VertexConsumer vc = multiBufferSource.getBuffer(RenderType.lines());

        LevelRenderer.renderLineBox(poseStack, vc, box, 0.0f, 1.0f, 0.0f, 1.0f);

        poseStack.popPose();
    }
}
