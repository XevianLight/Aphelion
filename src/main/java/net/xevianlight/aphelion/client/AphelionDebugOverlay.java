package net.xevianlight.aphelion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.client.dimension.DimensionRenderer;
import net.xevianlight.aphelion.client.dimension.DimensionRendererCache;
import net.xevianlight.aphelion.client.dimension.SpaceSkyEffects;

@EventBusSubscriber(modid = Aphelion.MOD_ID, value = Dist.CLIENT)
public class AphelionDebugOverlay {

    @SubscribeEvent
    public static void onDebugText(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Only show in your space dimension (optional)
        if (!mc.level.dimension().location().equals(ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "space"))) {
            return;
        }

        var camPos = mc.gameRenderer.getMainCamera().getPosition();
        ResourceLocation orbitId = SpaceSkyEffects.orbitForPos(camPos);

        DimensionRenderer r = DimensionRendererCache.getOrDefault(orbitId);

        String rendererSummary = (r == null)
                ? "<missing>"
                : ("customSky=" + r.customSky()
                + ", thickFog=" + r.hasThickFog()
                + ", fog=" + r.hasFog());

        // Left side of F3
        event.getLeft().add("");
        event.getLeft().add("Aphelion:");
        event.getLeft().add(" Orbit: " + orbitId);
        event.getLeft().add(" Sky: " + rendererSummary);
    }
}