package net.xevianlight.aphelion.block.dummy.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.xevianlight.aphelion.block.dummy.entity.BaseMultiblockDummyBlockEntity;
import net.xevianlight.aphelion.block.entity.custom.EAFPartEntity;

public class MultiblockDummyRenderer implements BlockEntityRenderer<BaseMultiblockDummyBlockEntity> {

    public MultiblockDummyRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(BaseMultiblockDummyBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        Level level = be.getLevel();
        if (level == null) return;

        BlockState mimic = be.getMimicing();           // <-- use stored state
        if (mimic == null) return;                     // or default to AIR/stone

        BlockRenderDispatcher brd = Minecraft.getInstance().getBlockRenderer();

        for (RenderType rt : brd.getBlockModel(mimic).getRenderTypes(mimic, level.random, ModelData.EMPTY)) {
            brd.renderBatched(
                    mimic,
                    be.getBlockPos(),
                    level,
                    poseStack,
                    buffer.getBuffer(rt),
                    false,
                    level.random,
                    ModelData.EMPTY,
                    rt
            );
        }
    }
}
