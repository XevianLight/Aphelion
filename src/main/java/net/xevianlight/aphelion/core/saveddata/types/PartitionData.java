package net.xevianlight.aphelion.core.saveddata.types;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.xevianlight.aphelion.planet.Planet;
import net.xevianlight.aphelion.planet.PlanetCache;
import net.xevianlight.aphelion.util.BigCodec;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PartitionData {
    public static final int MAX_PADS = 64;
    private static final StreamCodec<ByteBuf, List<BlockPos>> BLOCKPOS_LIST_CODEC = BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_PADS));

    @Nullable private ResourceLocation orbit;
    @Nullable private ResourceLocation destination;
    @Nullable private ResourceLocation system;
    private boolean traveling;
    /// How far we've already gone
    private double distanceTraveledAU;
    /// Total trip distance, from start to finish. Used with distanceTraveledAU to determine trip progress for UI or other. Not used in trip calculation.
    private double tripDistanceAU;
    private boolean generated;
    private UUID owner;
    private List<BlockPos> landingPadControllers;
    private List<BlockPos> engines;
    ///  Data object containing station rotation.
    private PosData posData;
    private double orbitDistance;

    ///  Cache the planet that corresponds to our orbit so we don't have to constantly look it up from PlanetCache. Will be accurate as long as setOrbit() is used exclusively.
    @Nullable private Planet cachedPlanet;
    ///  Cache the planet that corresponds to our destination so we don't have to constantly look it up from PlanetCache. Will be accurate as long as setDestination() is used exclusively.
    @Nullable private Planet cachedDestination;

    public PartitionData() {

    }

    public PartitionData(@Nullable ResourceLocation orbit) {
        setOrbit(orbit);
        setDestination(null);
        this.traveling = false;
        this.distanceTraveledAU = 0;
        this.tripDistanceAU = 0;
        this.generated = false;
        this.owner = null;
        this.landingPadControllers = List.of();
        this.engines = new ArrayList<>(List.of());
        this.posData = new PosData();
        setOrbitDistance(1);
    }

    public PartitionData(PartitionData other) {
        this.orbit = other.orbit;
        this.cachedPlanet = other.cachedPlanet;
        this.destination = other.destination;
        this.cachedDestination = other.cachedDestination;
        this.traveling = other.traveling;
        this.distanceTraveledAU = other.distanceTraveledAU;
        this.tripDistanceAU = other.tripDistanceAU; // copy directly, no recalculation
        this.generated = other.generated;
        this.owner = other.owner;
        this.engines = other.getEngines(); // defensive copy
        this.landingPadControllers = other.getLandingPadControllers();
        this.posData = other.posData;
        this.orbitDistance = other.orbitDistance;
        // don't set dirty callback — caller must do that
    }

    public static final StreamCodec<ByteBuf, PartitionData> STREAM_CODEC =
            BigCodec.composite(
                    // orbit is nullable -> optional codec
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
                    d -> Optional.ofNullable(d.getOrbit()),

                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
                    d -> Optional.ofNullable(d.getDestination()),

                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
                    d -> Optional.ofNullable(d.getSystem()),

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

                    ByteBufCodecs.VAR_LONG,
                    PartitionData::getPosDataPacked,

                    ByteBufCodecs.DOUBLE,
                    PartitionData::getOrbitDistance,

                    (orbitOpt, destOpt, systemOpt, traveling, distTraveled, distToDest, ownerOpt, generated, controllers, engines, posData, distance) -> {
                        PartitionData data = new PartitionData(orbitOpt.orElse(null));
                        data.setDestination(destOpt.orElse(null));
                        data.setSystem(systemOpt.orElse(null));
                        data.traveling = traveling;
                        data.distanceTraveledAU = distTraveled;
                        data.tripDistanceAU = distToDest;
                        data.owner = ownerOpt.orElse(null);
                        data.generated = generated;
                        data.landingPadControllers = controllers;
                        data.engines = engines;
                        data.posData = PosData.unpacker(posData);
                        data.setOrbitDistance(distance);
                        return data;
                    }
            );

    private Long getPosDataPacked() {
        if (posData == null) posData = new PosData();
        return posData.pack();
    }

    public @Nullable ResourceLocation getOrbit() {
        return this.orbit;
    }

    public void setOrbit(@Nullable ResourceLocation orbit) {
        this.orbit = orbit;
        cachedPlanet = PlanetCache.getByOrbitOrNull(orbit);
        if (cachedPlanet != null && this.posData != null)
            setOrbitDistance(cachedPlanet.orbitDistance());
        recalculateTripDistAU();
        distanceTraveledAU = 0;
        markDirty();
    }

    public @Nullable ResourceLocation getDestination() {
        cachedDestination = PlanetCache.getOrNull(destination);
        return destination;
    }

    public void setDestination(@Nullable ResourceLocation destination) {
        this.destination = destination;
        cachedDestination = PlanetCache.getOrNull(destination);
        recalculateTripDistAU();
        distanceTraveledAU = 0;
        markDirty();
    }

    public @Nullable ResourceLocation getSystem() {
        return system;
    }

    public void setSystem(@Nullable ResourceLocation system) {
        this.system = system;
        markDirty();
    }

    public boolean isTraveling() {
        return traveling;
    }

    public void setTraveling(boolean traveling) {
        if (this.traveling == traveling) return;
        this.traveling = traveling;
        markDirty();
    }

    public double getDistanceTraveledAU() {
        return distanceTraveledAU;
    }

    public void setDistanceTraveledAU(double distanceTraveledAU) {
        this.distanceTraveledAU = distanceTraveledAU;
        markDirty();
    }

    public double recalculateTripDistAU() {
        var currentPlanet = PlanetCache.getByOrbitOrNull(orbit);
        if (currentPlanet == null) {
            markDirty();
            return -1;
        }

        var destPlanet = PlanetCache.getOrDefault(destination);

        var dist = destPlanet.orbitDistance() - currentPlanet.orbitDistance();
        this.tripDistanceAU = dist;
        markDirty();
        return dist;
    }

    public double getTripDistanceAU() {
        return tripDistanceAU;
    }

    public double getTripDeltaAU() {
        if (cachedDestination == null) return 0;
        return cachedDestination.orbitDistance() - orbitDistance;
    }

    public double getOrbitDistance() {
        return orbitDistance;
    }

    public void setOrbitDistance(double orbitDistance) {
        this.orbitDistance = orbitDistance;
        markDirty();
    }

    public void setTripDistanceAU(double tripDistanceAU) {
        this.tripDistanceAU = tripDistanceAU;
    }

    /**
     * Advances travel progress by the specified distance in AU.
     *
     * <p>Each call moves the station's current AU position toward the destination
     * planet's AU value by the given amount. If the step would overshoot, the
     * position is clamped to the destination exactly and arrival is triggered.</p>
     *
     * @param distance the distance to advance in astronomical units (AU)
     * @return {@code true} when the station has arrived at its destination,
     *         {@code false} if travel is still in progress.
     */
    public boolean travel(double distance) {
        if (cachedDestination == null) return false;
        if (cachedPlanet == null) return false;

        double delta = getTripDeltaAU();
        double step = distance * Math.signum(delta);

        if (Math.abs(delta) <= distance) {
            this.orbitDistance = (cachedDestination.orbitDistance());
            this.orbit = cachedDestination.orbit().location();
            this.cachedPlanet = cachedDestination;
            this.destination = null;
            this.cachedDestination = null;
            this.traveling = false;
            distanceTraveledAU = tripDistanceAU;

            markDirty();
            return true;
        } else {
            distanceTraveledAU += distance;
            this.orbitDistance += step;

            markDirty();
            return false;
        }
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
        markDirty();
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        markDirty();
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
        markDirty();
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
        if (landingPadControllers.remove(pos)) {
            markDirty();
            return true;
        }
        return false;
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
        markDirty();
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
            markDirty();
            return true;
        }
        return false;
    }

    public boolean removeEngine(BlockPos pos) {
        if (engines.remove(pos)) {
            markDirty();
            return true;
        }
        return false;
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
                && Double.compare(this.orbitDistance, that.orbitDistance) == 0
                && this.generated == that.generated
                && Objects.equals(this.owner, that.owner)
                && Objects.equals(this.engines, that.engines)
                && Objects.equals(this.landingPadControllers, that.landingPadControllers);
        /* tripDistanceAU intentionally excluded — it is a derived value computed from
         * orbit and destination, and may fluctuate due to recalculation timing.
         * It should never drive packet equality decisions. */
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
        markDirty();
        setLandingPadControllers(newList);
    }

    public PosData getPosData() {
        return posData;
    }

    public PosData getPosDataOrDefault() {
        if (posData == null) return new PosData();
        return posData;
    }

    public void setPosData(PosData posData) {
        markDirty();
        this.posData = posData;
    }

    private Runnable onDirty;

    public void setDirtyCallback(Runnable onDirty) {
        this.onDirty = onDirty;
    }

    private void markDirty() {
        if (onDirty != null) onDirty.run();
    }
}
