package net.xevianlight.aphelion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.client.dimension.DimensionRenderer;
import net.xevianlight.aphelion.client.dimension.DimensionRendererCache;
import net.xevianlight.aphelion.client.dimension.SpaceSkyEffects;
import net.xevianlight.aphelion.core.saveddata.SpacePartitionSavedData;
import net.xevianlight.aphelion.util.SpacePartitionHelper;

@EventBusSubscriber(modid = Aphelion.MOD_ID, value = Dist.CLIENT)
public class AphelionDebugOverlay {

    @SubscribeEvent
    public static void onDebugText(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

//        // Only show in your space dimension (optional)
//        if (!mc.level.dimension().location().equals(ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "space"))) {
//            return;
//        }

        DimensionType type = mc.level.dimensionType();
        ResourceLocation effectsId = type.effectsLocation();

        var camPos = mc.gameRenderer.getMainCamera();
        ResourceLocation orbitId = SpaceSkyEffects.resolvedId(effectsId, camPos);

        DimensionRenderer r = DimensionRendererCache.getOrDefault(orbitId);

        String rendererSummary = (r == null)
                ? "<missing>"
                : ("customSky=" + r.customSky()
                + ", thickFog=" + r.hasThickFog()
                + ", fog=" + r.hasFog());

        int x = SpacePartitionHelper.get(Math.floor(mc.player.position().x));
        int z = SpacePartitionHelper.get(Math.floor(mc.player.position().z));

        // Left side of F3
        event.getLeft().add("");
        event.getLeft().add("Aphelion:");
        event.getLeft().add(" Orbit: " + orbitId);
//        event.getLeft().add(" Sky: " + rendererSummary);
        event.getLeft().add(" Station: " +  x + " " + z + "   ID: " + SpacePartitionSavedData.pack(x,z));
        event.getLeft().add(" Station Destination:" + PartitionClientState.lastData().getDestination());
    }
}