package net.xevianlight.aphelion.core.saveddata;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.xevianlight.aphelion.core.saveddata.types.GravityData;
import net.xevianlight.aphelion.planet.Planet;
import net.xevianlight.aphelion.planet.PlanetCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Pattern:
 *  - World-level SavedData
 *  - Outer map keyed by section (chunkX, sectionY, chunkZ) packed into a long
 *  - Inner map keyed by localIndex (0..4095) -> packed int env value
 *
 * Sparse by design: blocks not present in the inner map are implicitly "default environment".
 */
public class GravitySavedData extends SavedData {

    private final Long2IntOpenHashMap gravityData = new Long2IntOpenHashMap();

    private static final String NAME = "aphelion_gravity";

    public static GravitySavedData create() {
        return new GravitySavedData();
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        int size = gravityData.size();
        long[] positions = new long[size];
        int[] data = new int[size];

        int i = 0;
        for (var e : gravityData.long2IntEntrySet()) {
            positions[i] = e.getLongKey();
            data[i] = e.getIntValue();
            i++;
        }

        tag.putLongArray("Position", positions);
        tag.putIntArray("Value", data);

        return tag;
    }

    public static GravitySavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        GravitySavedData data = create();

        if (!tag.contains("Position", Tag.TAG_LONG_ARRAY) || !tag.contains("Value", Tag.TAG_INT_ARRAY)) { return data; }

        long[] positions = tag.getLongArray("Position");
        int[] values = tag.getIntArray("Value");

        int length = Math.min(positions.length, values.length);

        data.gravityData.ensureCapacity(length);

        for (int i = 0; i < length; i++) {
            data.gravityData.put(positions[i], values[i]);
        }

        return data;
    }

    private static final int ABSENT = Integer.MIN_VALUE;

    public @Nullable GravityData getGravityRegionData (Level level, BlockPos center) {
        GravityData data = GravityData.unpack(gravityData.getOrDefault(center.asLong(), ABSENT));
        return data.pack() == ABSENT ? null : data;
    }

    public void removeGravityRegion (BlockPos pos) {
        gravityData.remove(pos.asLong());
    }

    public void setGravityRegion (BlockPos pos, GravityData data) {
        gravityData.put(pos.asLong(), data.pack());
    }



    /**
     * Returns the cumulative sum of the acceleration for all gravity regions that overlap this block position
     */
    public float getGravitySum (BlockPos pos) {
        float sum = 0;

        List<GravityData> regions = getGravityRegions(pos);

        for (var e : regions) {
            sum += e.getGravity();
        }

        return sum;
    }

    /**
     * Returns the strongest acceleration among all gravity regions that overlap this block position
     */
    public float getGravityMax (BlockPos pos) {
        float max = 0;

        List<GravityData> regions = getGravityRegions(pos);

        for (var e : regions) {
            var accel = e.getGravity();
            if (accel > max) max = accel;
        }

        return max;
    }

    /**
     * Returns a list of all gravity data objects overlapping this position. NOTE: does not contain position of the regions
     */
    public List<GravityData> getGravityRegions (BlockPos pos) {
        List<GravityData> regions = new ArrayList<>();
        for (var entry : gravityData.long2IntEntrySet()) {
            GravityData data = GravityData.unpack(entry.getIntValue());
            BlockPos center = BlockPos.of(entry.getLongKey());

            if (contains(pos, center, data.getRadius())) {
                regions.add(data);
            }
        }

        return regions;
    }


    private static boolean contains(BlockPos pos, BlockPos center, float radius) {
        float distanceSquared = ((center.getX() - pos.getX()) * (center.getX() - pos.getX())) + ((center.getY() - pos.getY()) * (center.getY() - pos.getY())) + ((center.getZ() - pos.getZ()) * (center.getZ() - pos.getZ()));
        return distanceSquared <= radius * radius;
    }

    private static float defaultGravity (Level level) {
        Planet planet = PlanetCache.getByDimensionOrNull(level.dimension());
        if (planet == null) return GravityData.DEFAULT_GRAVITY;
        return planet.gravity();
    }

    public static GravitySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(GravitySavedData::create, GravitySavedData::load),
                NAME
        );
    }

}