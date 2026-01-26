package net.xevianlight.aphelion.network;


import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.entites.vehicles.RocketEntity;
import net.xevianlight.aphelion.network.packet.RocketLaunchPayload;

import net.xevianlight.aphelion.client.AphelionClient;

@EventBusSubscriber(modid = Aphelion.MOD_ID)
public final class KeyNetwork {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // consumeClick makes it fire once per press, not every tick held
        if (AphelionClient.ROCKET_LAUNCH_KEY.consumeClick() && mc.player.getVehicle() instanceof RocketEntity rocket) {
            PacketDistributor.sendToServer(new RocketLaunchPayload(rocket.getId()));
        }
    }
}
