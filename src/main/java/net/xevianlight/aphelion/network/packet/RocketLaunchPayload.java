package net.xevianlight.aphelion.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RocketLaunchPayload(int rocketEntityId) implements CustomPacketPayload {

    public static final Type<RocketLaunchPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aphelion", "rocket_launch"));

    public static final StreamCodec<ByteBuf, RocketLaunchPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, RocketLaunchPayload::rocketEntityId,
                    RocketLaunchPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}