package net.xevianlight.aphelion.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xevianlight.aphelion.Aphelion;
import org.jetbrains.annotations.NotNull;

public record SetTravelingPayload(BlockPos computerPos, boolean traveling) implements CustomPacketPayload {
    public static final Type<SetTravelingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "set_traveling"));

    public static final StreamCodec<ByteBuf, SetTravelingPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    SetTravelingPayload::computerPos,
                    ByteBufCodecs.BOOL,
                    SetTravelingPayload::traveling,
                    SetTravelingPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
