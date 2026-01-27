package net.xevianlight.aphelion.network;


import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.core.space.SpacePartitionSavedData;
import net.xevianlight.aphelion.network.packet.PartitionPayload;
import net.xevianlight.aphelion.util.SpacePartitionHelper;

import java.util.HashSet;
import java.util.Set;
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
                PacketDistributor.sendToPlayer(sp, now);
                // Store this packet for later
                LAST_SENT.put(sp.getUUID(), now);
            }
        }
    }

    private static PartitionPayload computePartitionFor(ServerPlayer sp) {
        // convert player position to partition coords
        int px = (int)Math.floor(sp.getX() / SpacePartitionHelper.SIZE);
        int pz = (int)Math.floor(sp.getZ() / SpacePartitionHelper.SIZE);

        // Get the orbit for the partition the player is in and create a packet for it
        var orbit = SpacePartitionSavedData.get(sp.serverLevel()).getOrbitForPartition(px, pz);
        String orbitId = (orbit != null) ? orbit.toString() : "aphelion:orbit/default";

        return new PartitionPayload(orbitId);
    }
}
