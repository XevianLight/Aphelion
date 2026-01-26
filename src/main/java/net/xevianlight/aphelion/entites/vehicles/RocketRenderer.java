package net.xevianlight.aphelion.entites.vehicles;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.xevianlight.aphelion.util.RocketStructure;
import org.jetbrains.annotations.NotNull;

public class RocketRenderer extends EntityRenderer<RocketEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public RocketRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.blockRenderer = ctx.getBlockRenderDispatcher(); // ✅ correct source
        this.shadowRadius = 0.8f;
    }

    @Override
    public void render(@NotNull RocketEntity entity,
                       float entityYaw,
                       float partialTicks,
                       PoseStack poseStack,
                       @NotNull MultiBufferSource buffers,
                       int packedLight) {

        RocketStructure s = entity.getStructure();

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));

        for (int i = 0; i < s.size(); i++) {
            int p = s.packedPosAt(i);

            poseStack.pushPose();
            poseStack.translate(
                    RocketStructure.unpackX(p) - 0.5d,
                    RocketStructure.unpackY(p),
                    RocketStructure.unpackZ(p) - 0.5d
            );

            blockRenderer.renderSingleBlock(
                    s.stateAt(i),
                    poseStack,
                    buffers,
                    packedLight,
                    OverlayTexture.NO_OVERLAY
            );

            poseStack.popPose();
        }

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity entity) {
        // Not used by block rendering, but required.
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}

