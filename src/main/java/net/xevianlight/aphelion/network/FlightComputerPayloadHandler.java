package net.xevianlight.aphelion.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.xevianlight.aphelion.block.entity.custom.StationFlightComputerBlockEntity;
import net.xevianlight.aphelion.client.DestinationClientCache;
import net.xevianlight.aphelion.network.packet.AvailableDestinationsPayload;
import net.xevianlight.aphelion.network.packet.SetDestinationPayload;
import net.xevianlight.aphelion.network.packet.SetTravelingPayload;

public class FlightComputerPayloadHandler {

    // Runs on the CLIENT: caches the planet list so the screen has it immediately on open.
    public static void handleAvailableDestinations(AvailableDestinationsPayload payload, IPayloadContext context) {
        DestinationClientCache.set(payload.planets());
    }

    // Runs on the SERVER: client-side button sends this; server commits it to PartitionData.
    public static void handleSetDestination(SetDestinationPayload payload, IPayloadContext context) {
        var level = context.player().level();
        if (level.getBlockEntity(payload.computerPos()) instanceof StationFlightComputerBlockEntity be) {
            be.setDestination(payload.destination().orElse(null));
        }
    }

    // Runs on the SERVER: the Launch/Abort button toggles traveling via this packet.
    public static void handleSetTraveling(SetTravelingPayload payload, IPayloadContext context) {
        var level = context.player().level();
        if (level.getBlockEntity(payload.computerPos()) instanceof StationFlightComputerBlockEntity be) {
            be.setTraveling(payload.traveling());
        }
    }
}
