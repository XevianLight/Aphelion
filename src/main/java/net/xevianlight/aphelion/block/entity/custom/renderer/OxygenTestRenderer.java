package net.xevianlight.aphelion.block.entity.custom.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.xevianlight.aphelion.block.entity.custom.OxygenTestBlockEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;

public class OxygenTestRenderer implements BlockEntityRenderer<OxygenTestBlockEntity> {
    public OxygenTestRenderer (BlockEntityRendererProvider.Context context) {}

    private List<BlockPos> toBlockPositions(OxygenTestBlockEntity be) {
        return be.getEnclosedBlocks();
    }

    @Override
    public AABB getRenderBoundingBox(OxygenTestBlockEntity blockEntity) {
        return AABB.ofSize(blockEntity.getBlockPos().getCenter(), OxygenTestBlockEntity.MAX_RANGE*2, OxygenTestBlockEntity.MAX_RANGE*2, OxygenTestBlockEntity.MAX_RANGE*2);
    }

    private Set<BlockPos> relativePositionsCache;
    @Override
    // If in debug mode, renders a model made from the blocks
    // that are currently returned by toBlockPositions(OxygenTestBlockEntity).
    public void render(OxygenTestBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // This bit's debug only, folks!
        if (!Minecraft.getInstance().gui.getDebugOverlay().showDebugScreen()) return;

        // Renderers are relative to our block pos, so transform all points to be relative to block pos as well
        List<BlockPos> positionsToRender = toBlockPositions(be);
        BlockPos originPos = be.getBlockPos();
        if (true) return;

        Set<BlockPos> relativePositions;
        if (relativePositionsCache != null) relativePositions = relativePositionsCache;
        else relativePositions = positionsToRender.stream().map(bp -> bp.offset(new Vec3i(-originPos.getX(), -originPos.getY(), -originPos.getZ()))).collect(Collectors.toSet());

        poseStack.pushPose();
        Matrix4f mat = poseStack.last().pose();
        VertexConsumer buf = buffer.getBuffer(RenderType.LIGHTNING); // for slightly transparent, lighting-ignoring textures (?), from what I already tested
        for (BlockPos p1 : relativePositions) {
            // Render a face for every side this block cannot find a neighbor in
            boolean upUncovered = !relativePositions.contains(p1.relative(Direction.UP));
            boolean downUncovered = !relativePositions.contains(p1.relative(Direction.DOWN));
            boolean northUncovered = !relativePositions.contains(p1.relative(Direction.NORTH));
            boolean southUncovered = !relativePositions.contains(p1.relative(Direction.SOUTH));
            boolean eastUncovered = !relativePositions.contains(p1.relative(Direction.EAST));
            boolean westUncovered = !relativePositions.contains(p1.relative(Direction.WEST));

            Vector3f centerPos = p1.getCenter().toVector3f();
            final Vector3f c000 = new Vector3f(centerPos).add(-0.500001f,-0.500001f,-0.500001f);
            final Vector3f c001 = new Vector3f(centerPos).add(0.500001f,-0.500001f,-0.500001f);
            final Vector3f c010 = new Vector3f(centerPos).add(-0.500001f,0.500001f,-0.500001f);
            final Vector3f c011 = new Vector3f(centerPos).add(0.500001f,0.500001f,-0.500001f);
            final Vector3f c100 = new Vector3f(centerPos).add(-0.500001f,-0.500001f,0.500001f);
            final Vector3f c101 = new Vector3f(centerPos).add(0.500001f,-0.500001f,0.500001f);
            final Vector3f c110 = new Vector3f(centerPos).add(-0.500001f,0.500001f,0.500001f);
            final Vector3f c111 = new Vector3f(centerPos).add(0.500001f,0.500001f,0.500001f);

            if (upUncovered) {
                addQuad(buf, mat, c110, c111, c011, c010, 0x6666FF);
            }
            if (downUncovered) {
                addQuad(buf, mat, c000, c001, c101, c100, 0x6666FF);
            }
            if (northUncovered) {
                addQuad(buf, mat, c001, c000, c010, c011, 0x6666FF);
            }
            if (eastUncovered) {
                addQuad(buf, mat, c101, c001, c011, c111, 0x6666FF);
            }
            if (southUncovered) {
                addQuad(buf, mat, c101, c111, c110, c100, 0x6666FF);
            }
            if (westUncovered) {
                addQuad(buf, mat, c100, c110, c010, c000, 0x6666FF);
            }
        }
        poseStack.popPose();
    }

    private void addQuad(VertexConsumer buf, Matrix4f matrix, Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3, int hexColor) {
        addVertex(buf, matrix, p0, hexColor);
        addVertex(buf, matrix, p1, hexColor);
        addVertex(buf, matrix, p2, hexColor);
        addVertex(buf, matrix, p3, hexColor);
    }

    private void addVertex(VertexConsumer buf, Matrix4f matrix, Vector3f vec, int hexColor) {
        float x = vec.x();
        float y = vec.y();
        float z = vec.z();
        int r = (hexColor & 0xFF0000) >> 16;
        int g = (hexColor & 0x00FF00) >> 8;
        int b = hexColor & 0x0000FF;
        buf.addVertex(matrix, x, y, z)
                .setColor(r, g, b, 255)
                .setUv(0, 0) // No texture, so this gets ignored
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880) //0xF000F0, or fully bright(?)
                .setNormal(0, 1, 0); // Facing upwards
    }
}
