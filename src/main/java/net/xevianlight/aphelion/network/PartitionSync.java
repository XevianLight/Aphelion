package net.xevianlight.aphelion.network;


import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.core.saveddata.SpacePartitionSavedData;
import net.xevianlight.aphelion.core.saveddata.types.PartitionData;
import net.xevianlight.aphelion.network.packet.PartitionPayload;
import net.xevianlight.aphelion.util.SpacePartition;

import java.util.UUID;

@EventBusSubscriber(modid = Aphelion.MOD_ID)
public final class PartitionSync {

    // Stora all packets we send to all players in a map so we can look it up later
    private static final java.util.Map<UUID, PartitionPayload> LAST_SENT = new java.util.HashMap<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        var server = e.getServer();

//        Aphelion.LOGGER.info("WORKS!!!");

        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {

            // Prepare a new packet and compare it with the last one we sent the player
            PartitionPayload now = computePartitionFor(sp);
            PartitionPayload prev = LAST_SENT.get(sp.getUUID());

            // If it is different, send them the new one
            if (prev == null || !prev.equals(now)) {
                Aphelion.LOGGER.debug("Partition changed for {}: prev={} now={}", sp.getName().getString(),
                        prev == null ? "null" : String.format("orbit=%s dest=%s traveling=%s distTraveled=%s tripDist=%s orbitDist=%s",
                                prev.partitionData().getOrbit(), prev.partitionData().getDestination(), prev.partitionData().isTraveling(),
                                prev.partitionData().getDistanceTraveledAU(), prev.partitionData().getTripDistanceAU(), prev.partitionData().getOrbitDistance()),
                        String.format("orbit=%s dest=%s traveling=%s distTraveled=%s tripDist=%s orbitDist=%s",
                                now.partitionData().getOrbit(), now.partitionData().getDestination(), now.partitionData().isTraveling(),
                                now.partitionData().getDistanceTraveledAU(), now.partitionData().getTripDistanceAU(), now.partitionData().getOrbitDistance())
                );
                PacketDistributor.sendToPlayer(sp, now);
                LAST_SENT.put(sp.getUUID(), now);
            }
        }
    }

    private static PartitionPayload computePartitionFor(ServerPlayer sp) {
        int px = (int)Math.floor(sp.getX() / SpacePartition.SIZE);
        int pz = (int)Math.floor(sp.getZ() / SpacePartition.SIZE);

        PartitionData live = SpacePartitionSavedData.get(sp.serverLevel()).getData(px, pz);

        // snapshot so mutations later don’t affect cached payloads
        PartitionData snapshot = (live == null) ? null : new PartitionData(live);

        return new PartitionPayload(snapshot);
    }
}
