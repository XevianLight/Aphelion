package net.xevianlight.aphelion.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.entites.vehicles.RocketEntity;
import net.xevianlight.aphelion.network.packet.RocketLaunchPayload;

public final class RocketPayloadHandlers {

    public static void handleRocketLaunch(final RocketLaunchPayload payload, final IPayloadContext ctx) {
        Aphelion.LOGGER.info("Rocket launch command received");

        // Ensure we run on the server thread
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            var level = sp.serverLevel();
            var e = level.getEntity(payload.rocketEntityId());
            if (!(e instanceof RocketEntity rocket)) return;

            // Security: only allow if the sender is actually riding this rocket
            if (sp.getVehicle() != rocket) return;

            // Start launch
            if (rocket.getPhase() == RocketEntity.FlightPhase.IDLE
                    || rocket.getPhase() == RocketEntity.FlightPhase.LANDED) {
                rocket.launch();
            }
        });
    }
}
