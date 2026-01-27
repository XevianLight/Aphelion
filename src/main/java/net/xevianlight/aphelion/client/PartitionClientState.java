package net.xevianlight.aphelion.client;

import net.xevianlight.aphelion.network.packet.PartitionPayload;

import java.util.Optional;

public final class PartitionClientState {
    private static volatile PartitionPayload last = null;

    public static void set(PartitionPayload d) { last = d; }

    public static Optional<PartitionPayload> get() {
        return Optional.ofNullable(last);
    }

    public static String idOrUnknown() {
        return last != null ? last.id() : "unknown";
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