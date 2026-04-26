package net.xevianlight.aphelion.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record PlanetInfo(ResourceLocation id, ResourceLocation orbit, double orbitDistance, Optional<ResourceLocation> parentPlanet) {

    public static final StreamCodec<ByteBuf, PlanetInfo> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, PlanetInfo::id,
            ResourceLocation.STREAM_CODEC, PlanetInfo::orbit,
            ByteBufCodecs.DOUBLE, PlanetInfo::orbitDistance,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), PlanetInfo::parentPlanet,
            (id, orb, dist, parent) -> new PlanetInfo(id, orb, dist, parent)
    );

    public boolean isMoon() {
        return parentPlanet.isPresent();
    }
}
