package net.xevianlight.aphelion.client;

import net.xevianlight.aphelion.core.saveddata.types.PartitionData;
import net.xevianlight.aphelion.network.packet.PartitionPayload;

import java.util.Optional;

public final class PartitionClientState {
    private static volatile PartitionPayload last = null;

    public static void set(PartitionPayload d) { last = d; }

    public static Optional<PartitionPayload> get() {
        return Optional.ofNullable(last);
    }

    public static String idOrUnknown() {
        String orbit = String.valueOf(last.partitionData().getOrbit());
        if (orbit == null) {
            return "aphleion:orbit/default";
        }
        return last != null ? orbit : "unknown";
    }

    public static PartitionData lastData() {
        return last.partitionData();
    }
//
//    public static int pxOr(int fallback) {
//        return last != null ? last.px() : fallback;
//    }
//
//    public static int pyOr(int fallback) {
//        return last != null ? last.py() : fallback;
//    }
}