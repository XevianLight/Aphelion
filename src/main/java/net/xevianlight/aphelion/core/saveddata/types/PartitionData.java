package net.xevianlight.aphelion.core.saveddata.types;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class PartitionData {
    @Nullable private ResourceLocation orbit;
    @Nullable private ResourceLocation destination;
    private boolean traveling;
    private double distanceTraveled;
    private double distanceToDest;

    public PartitionData(@Nullable ResourceLocation orbit) {
        this.orbit = orbit;
        this.destination = null;
        this.traveling = false;
        this.distanceTraveled = 0;
        this.distanceToDest = 0;
    }

    public PartitionData(PartitionData other) {
        this.orbit = other.orbit;
        this.destination = other.destination;
        this.traveling = other.traveling;
        this.distanceTraveled = other.distanceTraveled;
        this.distanceToDest = other.distanceToDest;
    }

    public static final StreamCodec<ByteBuf, PartitionData> STREAM_CODEC =
            StreamCodec.composite(
                    // orbit is nullable -> optional codec
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
                    d -> Optional.ofNullable(d.getOrbit()),

                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
                    d -> Optional.ofNullable(d.getDestination()),

                    ByteBufCodecs.BOOL,
                    PartitionData::isTraveling,

                    // doubles -> DOUBLE codec
                    ByteBufCodecs.DOUBLE,
                    PartitionData::getDistanceTraveled,

                    ByteBufCodecs.DOUBLE,
                    PartitionData::getDistanceToDest,

                    (orbitOpt, destOpt, traveling, distTraveled, distToDest) -> {
                        PartitionData data = new PartitionData(orbitOpt.orElse(null));
                        data.destination = destOpt.orElse(null);
                        data.traveling = traveling;
                        data.distanceTraveled = distTraveled;
                        data.distanceToDest = distToDest;
                        return data;
                    }
            );

    public @Nullable ResourceLocation getOrbit() {
        return this.orbit;
    }

    public void setOrbit(ResourceLocation orbit) {
        this.orbit = orbit;
    }

    public @Nullable ResourceLocation getDestination() {
        return destination;
    }

    public void setDestination(@Nullable ResourceLocation destination) {
        this.destination = destination;
    }

    public boolean isTraveling() {
        return traveling;
    }

    public void setTraveling(boolean traveling) {
        this.traveling = traveling;
    }

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    public void setDistanceTraveled(double distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }

    public double getDistanceToDest() {
        return distanceToDest;
    }

    public void setDistanceToDest(double distanceToDest) {
        this.distanceToDest = distanceToDest;
    }

    public void travel(double distance) {
        distanceTraveled = Math.min( distanceTraveled + distance, distanceToDest);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        PartitionData that = (PartitionData) obj;

        return Objects.equals(this.orbit, that.orbit)
                && Objects.equals(this.destination, that.destination)
                && this.traveling == that.traveling
                && Double.compare(this.distanceTraveled, that.distanceTraveled) == 0
                && Double.compare(this.distanceToDest, that.distanceToDest) == 0;
    }
}
