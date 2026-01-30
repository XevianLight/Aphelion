package net.xevianlight.aphelion.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.core.saveddata.types.PartitionData;

import java.util.Objects;

public record PartitionPayload(PartitionData partitionData) implements CustomPacketPayload {
    public static final Type<PartitionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "partition_data"));

    public static final StreamCodec<ByteBuf, PartitionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    PartitionData.STREAM_CODEC,
                    PartitionPayload::partitionData,
                    PartitionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PartitionPayload that = (PartitionPayload) o;
        return partitionData.equals(that.partitionData);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(partitionData);
    }
}
