package net.xevianlight.aphelion.core.saveddata;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.core.saveddata.types.PartitionData;
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
                pd.setDistanceTraveled(e.getDouble("DistanceTraveled"));
            }
            if (e.contains("DistanceToDest", CompoundTag.TAG_DOUBLE)) {
                pd.setDistanceToDest(e.getDouble("DistanceToDest"));
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

            e.putDouble("DistanceTraveled", pd.getDistanceTraveled());
            e.putDouble("DistanceToDest", pd.getDistanceToDest());

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

    public @Nullable PartitionData getData(int px, int pz) {
        long key = pack(px, pz);
        PartitionData data = map.get(key);
        if (data == null) {
            // pick a sensible default orbit, or null if you truly allow it
            data = new PartitionData(Aphelion.id("orbit/default"));
            map.put(key, data);
            setDirty();
        }
        return data;
    }

    public void overwriteAllExistingOrbits(ResourceLocation orbit) {
        if (map.isEmpty()) return;

        boolean changed = false;
        for (var entry : map.long2ObjectEntrySet()) {
            if(!orbit.equals(entry.getValue())) {
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
