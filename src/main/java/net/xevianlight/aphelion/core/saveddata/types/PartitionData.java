package net.xevianlight.aphelion.core.saveddata.types;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.xevianlight.aphelion.planet.PlanetCache;
import net.xevianlight.aphelion.util.BigCodec;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PartitionData {
    public static final int MAX_PADS = 64;
    private static final StreamCodec<ByteBuf, List<BlockPos>> BLOCKPOS_LIST_CODEC = BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_PADS));

    @Nullable private ResourceLocation orbit;
    @Nullable private ResourceLocation destination;
    private boolean traveling;
    /// How far we've already gone
    private double distanceTraveledAU;
    /// Total trip distance, from start to finish
    private double tripDistanceAU;
    private boolean generated;
    private UUID owner;
    private List<BlockPos> landingPadControllers;
    private List<BlockPos> engines;
    private double currentOrbitDistanceAU;

    public PartitionData() {

    }

    public PartitionData(@Nullable ResourceLocation orbit) {
        this.orbit = orbit;
        this.destination = null;
        this.traveling = false;
        this.distanceTraveledAU = 0;
        this.tripDistanceAU = 0;
        this.generated = false;
        this.owner = null;
        this.landingPadControllers = List.of();
        this.engines = new ArrayList<>(List.of());
    }

    public PartitionData(PartitionData other) {
        this.orbit = other.orbit;
        this.destination = other.destination;
        this.traveling = other.traveling;
        this.distanceTraveledAU = other.distanceTraveledAU;
        this.tripDistanceAU = other.tripDistanceAU;
        this.generated = other.generated;
        this.owner = other.owner;
        this.landingPadControllers = other.landingPadControllers;
        this.engines = other.engines;
    }

    public static final StreamCodec<ByteBuf, PartitionData> STREAM_CODEC =
            BigCodec.composite(
                    // orbit is nullable -> optional codec
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
                    d -> Optional.ofNullable(d.getOrbit()),

                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
                    d -> Optional.ofNullable(d.getDestination()),

                    ByteBufCodecs.BOOL,
                    PartitionData::isTraveling,

                    // doubles -> DOUBLE codec
                    ByteBufCodecs.DOUBLE,
                    PartitionData::getDistanceTraveledAU,

                    ByteBufCodecs.DOUBLE,
                    PartitionData::recalculateTripDistAU,

                    ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
                    d -> Optional.ofNullable(d.getOwner()),

                    ByteBufCodecs.BOOL,
                    PartitionData::isGenerated,

                    BLOCKPOS_LIST_CODEC,
                    PartitionData::getLandingPadControllers,

                    BLOCKPOS_LIST_CODEC,
                    PartitionData::getEngines,

                    (orbitOpt, destOpt, traveling, distTraveled, distToDest, ownerOpt, generated, controllers, engines) -> {
                        PartitionData data = new PartitionData(orbitOpt.orElse(null));
                        data.destination = destOpt.orElse(null);
                        data.traveling = traveling;
                        data.distanceTraveledAU = distTraveled;
                        data.tripDistanceAU = distToDest;
                        data.owner = ownerOpt.orElse(null);
                        data.generated = generated;
                        data.landingPadControllers = controllers;
                        data.engines = engines;
                        return data;
                    }
            );

    public @Nullable ResourceLocation getOrbit() {
        return this.orbit;
    }

    public void setOrbit(@Nullable ResourceLocation orbit) {
        this.orbit = orbit;
        recalculateTripDistAU();
        distanceTraveledAU = 0;
    }

    public @Nullable ResourceLocation getDestination() {
        return destination;
    }

    public void setDestination(@Nullable ResourceLocation destination) {
        this.destination = destination;
        recalculateTripDistAU();
        distanceTraveledAU = 0;
    }

    public boolean isTraveling() {
        return traveling;
    }

    public void setTraveling(boolean traveling) {
        this.traveling = traveling;
    }

    public double getDistanceTraveledAU() {
        return distanceTraveledAU;
    }

    public void setDistanceTraveledAU(double distanceTraveledAU) {
        this.distanceTraveledAU = distanceTraveledAU;
    }

    public double recalculateTripDistAU() {
        var currentPlanet = PlanetCache.getByOrbitOrNull(orbit);
        if (currentPlanet == null) {
            return -1;
        }

        var destPlanet = PlanetCache.getOrDefault(destination);

        var dist = destPlanet.orbitDistance() - currentPlanet.orbitDistance();
        this.tripDistanceAU = dist;
        return dist;
    }

    public double getTripDistanceAU() {
        return tripDistanceAU;
    }

    public void setTripDistanceAU(double tripDistanceAU) {
        this.tripDistanceAU = tripDistanceAU;
    }

    /**
     * Advances travel progress by the specified distance in AU.
     *
     * <p>This increases {@code distanceTraveledAU} by the given amount and clamps
     * the result so it never exceeds {@code tripDistanceAU}.</p>
     *
     * <p>If the requested distance would overshoot the destination, the traveled
     * distance is set to exactly {@code tripDistanceAU}.</p>
     *
     * @param distance the distance to advance in astronomical units (AU)
     * @return {@code true} when we arrive at our destination, {@code false} otherwise.
     */
    public boolean travel(double distance) {
        if (distanceTraveledAU + distance > tripDistanceAU) {
            distanceTraveledAU = tripDistanceAU;
            var destinationPlanet = PlanetCache.getOrNull(destination);
            if (destinationPlanet != null) {
                setOrbit(destinationPlanet.orbit().location());
            }
            return false;
        } else {
            distanceTraveledAU += distance;
            return true;
        }
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
    }

    /**
     * Returns a copy of the world positions of all landing pad controllers tracked
     * by this partition.
     *
     * <p>This method returns only the stored {@link BlockPos} locations of known
     * landing pad controllers, not the controller instances or block entities
     * themselves. To interact with a controller, retrieve its block entity from
     * the world using the returned positions.</p>
     *
     * <p>The returned list is a defensive copy and may be modified without affecting
     * the underlying partition data. To persist changes, use
     * {@code setLandingPadControllers(...)}.</p>
     *
     * @return a mutable copy of the landing pad controller positions known to this partition
     */
    public List<BlockPos> getLandingPadControllers() {
        return new ArrayList<>(landingPadControllers);
    }

    public void setLandingPadControllers(List<BlockPos> landingPadControllers) {
        this.landingPadControllers = landingPadControllers;
    }


    /**
     * Adds a landing pad controller at the specified world position.
     *
     * <p>If a controller does not already exist at the given position, it is added
     * to the internal collection and the method returns {@code true}. If a controller
     * is already present at that position, no changes are made.</p>
     *
     * @param pos the world position of the landing pad controller to add
     * @return {@code true} if the controller was added, {@code false} if it already existed
     */
    public boolean addLandingPadController(BlockPos pos) {
        if (!landingPadControllers.contains(pos)) {
            landingPadControllers.add(pos);
            return true;
        }
        return false;
    }

    /**
     * Removes the landing pad controller at the specified world position.
     *
     * <p>If a controller exists at the given position, it is removed from the
     * internal collection and the method returns {@code true}. If no controller
     * is present at that position, no changes are made.</p>
     *
     * @param pos the world position of the landing pad controller to remove
     * @return {@code true} if a controller was removed, {@code false} otherwise
     */
    public boolean removeLandingPadController(BlockPos pos) {
        return landingPadControllers.remove(pos);
    }

    /**
     * Returns a defensive copy of the world positions of all engines tracked by this partition.
     *
     * <p>This method returns only the stored {@link BlockPos} locations of known engines,
     * not the engine instances or their corresponding block entities. To interact with an
     * engine, retrieve the block entity from the world using the returned positions.</p>
     *
     * <p>It is not guaranteed that an engine exists at every returned position. If changes
     * fail to synchronize with this partition, the stored data may become inaccurate.
     * Always verify that the block entity at a given position is an engine before use.</p>
     *
     * <p>The returned list is a defensive copy and may be modified without affecting the
     * underlying partition data. To persist changes, use {@code setEngines(...)}.</p>
     *
     * @return a mutable list containing the tracked engine positions
     */
    public List<BlockPos> getEngines() {
        return new ArrayList<>(engines);
    }

    public void setEngines(List<BlockPos> engines) {
        this.engines = engines;
    }

    /**
     * Adds an engine at the specified world position.
     *
     * <p>If an engine does not already exist at the given position, it is added
     * to the internal collection and the method returns {@code true}. If an engine
     * is already present at that position, no changes are made.</p>
     *
     * @param pos the world position of the engine to add
     * @return {@code true} if the engine was added, {@code false} if it already existed
     */
    public boolean addEngine(BlockPos pos) {
        if (!engines.contains(pos)) {
            engines.add(pos);
            return true;
        }
        return false;
    }

    public boolean removeEngine(BlockPos pos) {
        return engines.remove(pos);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        PartitionData that = (PartitionData) obj;

        return Objects.equals(this.orbit, that.orbit)
                && Objects.equals(this.destination, that.destination)
                && this.traveling == that.traveling
                && Double.compare(this.distanceTraveledAU, that.distanceTraveledAU) == 0
                && Double.compare(this.tripDistanceAU, that.tripDistanceAU) == 0
                && this.generated == that.generated
                && Objects.equals(this.owner, that.owner);
    }

    public long[] getLandingPadContollersAsArray() {
        long[] out = new long[landingPadControllers.size()];
        int i = 0;
        for (BlockPos pos : landingPadControllers) {
            out[i] = pos.asLong();
            i++;
        }
        return out;
    }

    public void setLandingPadContollersFromArray(long[] in) {
        List<BlockPos> newList = new java.util.ArrayList<>(List.of());
        int i = 0;
        for (Long packedPos : in) {
            newList.add(BlockPos.of(packedPos));
            i++;
        }
        setLandingPadControllers(newList);
    }
}
