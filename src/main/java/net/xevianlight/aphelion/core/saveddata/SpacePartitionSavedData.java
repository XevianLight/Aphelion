package net.xevianlight.aphelion.core.saveddata;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.core.saveddata.types.PartitionData;
import net.xevianlight.aphelion.core.saveddata.types.PosData;
import net.xevianlight.aphelion.util.SpacePartition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpacePartitionSavedData extends SavedData {

    private static final String NAME = "aphelion_station_partitions";

    private final Long2ObjectMap<PartitionData> map = new Long2ObjectOpenHashMap<>();

    public static SpacePartitionSavedData create() {
        return new SpacePartitionSavedData();
    }

    public static SpacePartitionSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        SpacePartitionSavedData data = create();

        ListTag entries = tag.getList("Entries", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag e = entries.getCompound(i);

            long key = e.getLong("Key");

            ResourceLocation orbitRL = null;
            if (e.contains("Orbit", CompoundTag.TAG_STRING)) {
                orbitRL = ResourceLocation.tryParse(e.getString("Orbit"));
            }

            PartitionData pd = new PartitionData(orbitRL);

            // Destination (optional)
            if (e.contains("Destination", CompoundTag.TAG_STRING)) {
                ResourceLocation destRL = ResourceLocation.tryParse(e.getString("Destination"));
                pd.setDestination(destRL); // ok if null (parse fail)
            } else {
                pd.setDestination(null);
            }

            // Traveling (optional; default false)
            if (e.contains("Traveling", CompoundTag.TAG_BYTE)) {
                pd.setTraveling(e.getBoolean("Traveling"));
            }

            // Distances (optional; default 0.0)
            if (e.contains("DistanceTraveled", CompoundTag.TAG_DOUBLE)) {
                pd.setDistanceTraveledAU(e.getDouble("DistanceTraveled"));
            }
            if (e.contains("DistanceToDest", CompoundTag.TAG_DOUBLE)) {
                pd.setTripDistanceAU(e.getDouble("DistanceToDest"));
            }

            if (e.hasUUID("Owner")) {
                pd.setOwner(e.getUUID("Owner"));
            }

            if (e.contains("Generated", CompoundTag.TAG_BYTE)) {
                pd.setGenerated(e.getBoolean("Generated"));
            }

            if (e.contains("LandingPads", CompoundTag.TAG_LONG_ARRAY)) {
                pd.setLandingPadContollersFromArray(e.getLongArray("LandingPads"));
            }

            if (e.contains("PosData", CompoundTag.TAG_LONG)) {
                pd.setPosData(PosData.unpacker(e.getLong("PosData")));
            }

            if (e.contains("OrbitDistance", CompoundTag.TAG_DOUBLE)) {
                pd.setOrbitDistance(e.getDouble("OrbitDistance"));
            }

            data.map.put(key, pd);
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        ListTag entries = new ListTag();

        map.long2ObjectEntrySet().forEach(entry -> {
            long key = entry.getLongKey();
            PartitionData pd = entry.getValue();

            CompoundTag e = new CompoundTag();
            e.putLong("Key", key);

            // Orbit
            if (pd.getOrbit() != null) {
                e.putString("Orbit", pd.getOrbit().toString());
            }

            // Destination (only if present)
            if (pd.getDestination() != null) {
                e.putString("Destination", pd.getDestination().toString());
            }

            // Traveling + distances
            e.putBoolean("Traveling", pd.isTraveling());

            e.putDouble("DistanceTraveled", pd.getDistanceTraveledAU());
            e.putDouble("DistanceToDest", pd.recalculateTripDistAU());

            if (pd.getOwner() != null) {
                e.putUUID("Owner", pd.getOwner());
            }

            e.putBoolean("Generated", pd.isGenerated());

            e.putLongArray("LandingPads", pd.getLandingPadContollersAsArray());

            e.putLong("PosData", pd.getPosDataOrDefault().pack());

            e.putDouble("OrbitDistance", pd.getOrbitDistance());

            entries.add(e);
        });

        tag.put("Entries", entries);
        return tag;
    }


    public @Nullable ResourceLocation getOrbitForPartition(int px, int pz) {
        PartitionData data = map.get(pack(px, pz));
        if (data == null) return null;
        return map.get(pack(px, pz)).getOrbit();
    }

    public void setOrbitForPartition(int px, int pz, ResourceLocation orbit) {
        long key = pack(px, pz);
        PartitionData prev = map.get(key);
        PartitionData newData = new PartitionData(prev);
        newData.setOrbit(orbit);
        if (!newData.equals(prev)) {
            map.put(key, newData);
            setDirty();
        }
    }

    public boolean clearOrbitForPartition(int px, int pz) {
        long key = pack(px, pz);
        PartitionData removed = map.remove(key);
        if (removed != null) {
            setDirty();
            return true;
        }
        return false;
    }



    public void clearAllOrbits() {
        if (!map.isEmpty()) {
            map.clear();
            setDirty();
        }
    }

    /**
     * Returns the {@link PartitionData} stored at the given partition indices.
     *
     * <p>The returned {@code PartitionData} is mutable. Any modifications to the returned
     * object will be persisted to the server.</p>
     *
     * <p>If no {@code PartitionData} exists at the specified indices, a new instance is
     * created using {@code aphelion:orbit/default}, stored in the server cache, and returned.</p>
     *
     * @param px the partition X index
     * @param pz the partition Z index
     * @return the {@code PartitionData} associated with the specified partition indices
     */
    public @NotNull PartitionData getData(int px, int pz) {
        long key = pack(px, pz);
        PartitionData data = map.get(key);
        if (data == null) {

            // pick a sensible default orbit, or null if you truly allow it
            data = new PartitionData(Aphelion.id("orbit/unassigned"));
            map.put(key, data);
            setDirty();
        }
        data.setDirtyCallback(this::setDirty);
        return data;
    }

    /**
     * Returns the {@link PartitionData} for the partition containing the given world position.
     *
     * <p>The returned {@code PartitionData} is mutable. Any modifications to the returned
     * object will be persisted to the server.</p>
     *
     * <p>If no {@code PartitionData} exists for the partition containing the specified
     * position, a new instance is created using {@code aphelion:orbit/default},
     * stored in the server cache, and returned.</p>
     *
     * @param x the world X coordinate
     * @param z the world Z coordinate
     * @return the {@code PartitionData} associated with the partition containing the position
     */
    public @NotNull PartitionData getDataForPos(int x, int z) {
        int px = SpacePartition.get(x);
        int pz = SpacePartition.get(z);
        long key = pack(px, pz);
        PartitionData data = map.get(key);
        if (data == null) {

            data = new PartitionData(Aphelion.id("orbit/default"));
            map.put(key, data);
            setDirty();
        }
        data.setDirtyCallback(this::setDirty);
        return data;
    }

    /**
     * Returns the {@link PartitionData} for the partition containing the given block position.
     *
     * <p>The returned {@code PartitionData} is mutable. Any modifications to the returned
     * object will be persisted to the server.</p>
     *
     * <p>If no {@code PartitionData} exists for the partition containing the specified
     * position, a new instance is created using {@code aphelion:orbit/default},
     * stored in the server cache, and returned.</p>
     *
     * @param pos the world block position
     * @return the {@code PartitionData} associated with the partition containing the position
     */
    public @NotNull PartitionData getDataForBlockPos(BlockPos pos) {
        int px = SpacePartition.get(pos.getX());
        int pz = SpacePartition.get(pos.getZ());
        long key = pack(px, pz);
        PartitionData data = map.get(key);
        if (data == null) {

            data = new PartitionData(Aphelion.id("orbit/default"));
            map.put(key, data);
            setDirty();
        }
        data.setDirtyCallback(this::setDirty);
        return data;
    }

    public void overwriteAllExistingOrbits(ResourceLocation orbit) {
        if (map.isEmpty()) return;

        boolean changed = false;
        for (var entry : map.long2ObjectEntrySet()) {
            if(!orbit.equals(entry.getValue().getOrbit())) {
                entry.getValue().setOrbit(orbit);
                changed = true;
            }
        }

        if (changed) setDirty();
    }

    public static SpacePartitionSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(SpacePartitionSavedData::create, SpacePartitionSavedData::load),
                NAME
        );
    }

    public static long pack(int px, int pz) {
        return (((long) px) << 32) | (pz & 0xffffffffL);
    }

    public static int unpackX(long key) {
        return (int)(key >> 32);
    }

    public static int unpackZ(long key) {
        return (int)key;
    }

}
