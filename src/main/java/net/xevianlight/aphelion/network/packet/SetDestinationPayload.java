package net.xevianlight.aphelion.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xevianlight.aphelion.Aphelion;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record SetDestinationPayload(BlockPos computerPos, Optional<ResourceLocation> destination) implements CustomPacketPayload {
    public static final Type<SetDestinationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "set_destination"));

    public static final StreamCodec<ByteBuf, SetDestinationPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    SetDestinationPayload::computerPos,
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
                    SetDestinationPayload::destination,
                    SetDestinationPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
