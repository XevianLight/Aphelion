package net.xevianlight.aphelion.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xevianlight.aphelion.Aphelion;

public record PartitionPayload(String id) implements CustomPacketPayload {
    public static final Type<PartitionPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "partition_data"));

    public static final StreamCodec<ByteBuf, PartitionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            PartitionPayload::id,

            PartitionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
