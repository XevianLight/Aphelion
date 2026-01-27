package net.xevianlight.aphelion.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.client.PartitionClientState;
import net.xevianlight.aphelion.network.packet.PartitionPayload;

// Handle packets TO the client FROM the server
public class PartitionPayloadHandler {

    public static void handleDataOnMain(PartitionPayload data, IPayloadContext context) {
        // Set our local partition state to the packet we just received.
        PartitionClientState.set(data);
        Aphelion.LOGGER.info("Partition packet received! id={}", data.id());
    }
}
