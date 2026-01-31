package net.xevianlight.aphelion.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.xevianlight.aphelion.Aphelion;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = Aphelion.MOD_ID, value = Dist.CLIENT)
public final class OxygenDebugRender {

    // Untextured translucent quads (POSITION_COLOR only)
    private static final RenderType OXYGEN_FILL = RenderType.create(
            "aphelion_oxygen_fill",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .createCompositeState(true)
    );

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // One stage only (pick one that exists and looks good)
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!mc.gui.getDebugOverlay().showDebugScreen()) return;

        PoseStack poseStack = event.getPoseStack();
        var cam = mc.gameRenderer.getMainCamera();
        var camPos = cam.getPosition();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(OXYGEN_FILL);

        // Render surface faces only (fast + pretty)
        for (long l : ClientOxygenCache.OXYGEN) {
            BlockPos p = BlockPos.of(l);
            drawSurfaceFaces(poseStack, vc, p);
        }

        poseStack.popPose();
        bufferSource.endBatch(OXYGEN_FILL);
    }

    private static void drawSurfaceFaces(PoseStack poseStack, VertexConsumer vc, BlockPos p) {
        // Neighbor checks: only render faces exposed to non-oxygen
        boolean up    = ClientOxygenCache.OXYGEN.contains(p.above().asLong());
        boolean down  = ClientOxygenCache.OXYGEN.contains(p.below().asLong());
        boolean north = ClientOxygenCache.OXYGEN.contains(p.north().asLong());
        boolean south = ClientOxygenCache.OXYGEN.contains(p.south().asLong());
        boolean east  = ClientOxygenCache.OXYGEN.contains(p.east().asLong());
        boolean west  = ClientOxygenCache.OXYGEN.contains(p.west().asLong());

        if (up && down && north && south && east && west) return;

        final float eps = 0.0025f;
        float x0 = p.getX() + eps;
        float y0 = p.getY() + eps;
        float z0 = p.getZ() + eps;
        float x1 = p.getX() + 1 - eps;
        float y1 = p.getY() + 1 - eps;
        float z1 = p.getZ() + 1 - eps;

        // Color (ARGB-ish but as floats)
        float r = 0.2f, g = 0.8f, b = 1.0f, a = 0.18f;

        Matrix4f mat = poseStack.last().pose();

        // IMPORTANT: vertex winding should be consistent (counter-clockwise)
        if (!up)    quad(mat, vc, x0,y1,z0,  x1,y1,z0,  x1,y1,z1,  x0,y1,z1,  r,g,b,a);
        if (!down)  quad(mat, vc, x0,y0,z1,  x1,y0,z1,  x1,y0,z0,  x0,y0,z0,  r,g,b,a);

        if (!north) quad(mat, vc, x1,y0,z0,  x0,y0,z0,  x0,y1,z0,  x1,y1,z0,  r,g,b,a);
        if (!south) quad(mat, vc, x0,y0,z1,  x1,y0,z1,  x1,y1,z1,  x0,y1,z1,  r,g,b,a);

        if (!east)  quad(mat, vc, x1,y0,z1,  x1,y0,z0,  x1,y1,z0,  x1,y1,z1,  r,g,b,a);
        if (!west)  quad(mat, vc, x0,y0,z0,  x0,y0,z1,  x0,y1,z1,  x0,y1,z0,  r,g,b,a);
    }

    private static void quad(
            Matrix4f mat, VertexConsumer vc,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float r, float g, float b, float a
    ) {
        // POSITION_COLOR format: ONLY position + color.
        vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
        vc.addVertex(mat, x3, y3, z3).setColor(r, g, b, a);
    }
}
