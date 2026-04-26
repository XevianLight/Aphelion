package net.xevianlight.aphelion.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xevianlight.aphelion.Aphelion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AvailableDestinationsPayload(List<PlanetInfo> planets) implements CustomPacketPayload {
    public static final Type<AvailableDestinationsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "available_destinations"));

    // 256 is a hard cap on the list codec to prevent oversized packets; the solar system has ~8 planets
    public static final StreamCodec<ByteBuf, AvailableDestinationsPayload> STREAM_CODEC =
            PlanetInfo.STREAM_CODEC.apply(ByteBufCodecs.list(256))
                    .map(AvailableDestinationsPayload::new, AvailableDestinationsPayload::planets);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
