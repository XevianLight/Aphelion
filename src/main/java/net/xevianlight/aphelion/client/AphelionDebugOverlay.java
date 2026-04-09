package net.xevianlight.aphelion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.client.dimension.DimensionRenderer;
import net.xevianlight.aphelion.client.dimension.DimensionRendererCache;
import net.xevianlight.aphelion.client.dimension.SpaceSkyEffects;
import net.xevianlight.aphelion.core.saveddata.EnvironmentSavedData;
import net.xevianlight.aphelion.core.saveddata.SpacePartitionSavedData;
import net.xevianlight.aphelion.planet.Planet;
import net.xevianlight.aphelion.planet.PlanetCache;
import net.xevianlight.aphelion.util.SpacePartition;

import java.util.Arrays;

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

        int x = SpacePartition.get(Math.floor(mc.player.position().x));
        int z = SpacePartition.get(Math.floor(mc.player.position().z));

        ResourceLocation orbit = PartitionClientState.lastData().getOrbit();
        Planet planet = PlanetCache.getByOrbitOrDefault(orbit);
        var dimension = planet.dimension();

        // Left side of F3
        event.getLeft().add("");
        event.getLeft().add("Aphelion:");
        event.getLeft().add(" Orbit: " + orbit);
        event.getLeft().add(" Planet: " + PlanetCache.getByOrbitOrNull(orbit));
        event.getLeft().add(" Associated Dimension: " + dimension.location().toString());
//        event.getLeft().add(" Sky: " + rendererSummary);
        event.getLeft().add(" Station: " +  x + " " + z + "   ID: " + SpacePartitionSavedData.pack(x,z));
        event.getLeft().add(" Station Destination: " + PartitionClientState.lastData().getDestination());
        event.getLeft().add(" Station Destination AU: " + PlanetCache.getOrDefault(PartitionClientState.lastData().getDestination()).orbitDistance());
        event.getLeft().add(" Station Owner: " + PartitionClientState.lastData().getOwner());
        event.getLeft().add(" Station Engines: " + Arrays.toString(PartitionClientState.lastData().getEngines().toArray()));
        event.getLeft().add(" Station Landing Pads: " + PartitionClientState.lastData().getLandingPadContollersAsArray().length);
        event.getLeft().add(" Station Traveling: " + PartitionClientState.lastData().isTraveling());
        event.getLeft().add(" Station Orbital Distance AU: " + PartitionClientState.lastData().getOrbitDistance());
        event.getLeft().add(" Station Trip Distance AU: " + PartitionClientState.lastData().getTripDistanceAU());
        event.getLeft().add(" Station Trip Traveled AU: " + PartitionClientState.lastData().getDistanceTraveledAU());
        event.getLeft().add(" Station PosData: " + PartitionClientState.lastData().getPosData().toString());
        var server = mc.getSingleplayerServer();
        ServerLevel singlePlayerLevel;
        if (server != null) {
            singlePlayerLevel = server.getLevel(mc.level.dimension());
            if (singlePlayerLevel != null)
                event.getLeft().add(" Oxygen: " + EnvironmentSavedData.get(singlePlayerLevel).hasOxygen(singlePlayerLevel, mc.player.blockPosition().mutable().above()));
        }
    }
}