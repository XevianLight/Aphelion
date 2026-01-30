package net.xevianlight.aphelion.core.saveddata;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.xevianlight.aphelion.core.saveddata.types.EnvironmentData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Pattern:
 *  - World-level SavedData
 *  - Outer map keyed by section (chunkX, sectionY, chunkZ) packed into a long
 *  - Inner map keyed by localIndex (0..4095) -> packed int env value
 *
 * Sparse by design: blocks not present in the inner map are implicitly "default environment".
 */
public class EnvironmentSavedData extends SavedData {

    private final Long2IntOpenHashMap envData = new Long2IntOpenHashMap();

    private static final String NAME = "aphelion_environment";

    public static EnvironmentSavedData create() {
        return new EnvironmentSavedData();
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        int size = envData.size();
        long[] positions = new long[size];
        int[] data = new int[size];

        int i = 0;
        for (var e : envData.long2IntEntrySet()) {
            positions[i] = e.getLongKey();
            data[i] = e.getIntValue();
            i++;
        }

        tag.putLongArray("Position", positions);
        tag.putIntArray("Value", data);

        return tag;
    }

    public static EnvironmentSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        EnvironmentSavedData data = create();

        if (!tag.contains("Position", Tag.TAG_LONG_ARRAY) || !tag.contains("Value", Tag.TAG_INT_ARRAY)) { return data; }

        long[] positions = tag.getLongArray("Positions");
        int[] values = tag.getIntArray("Value");

        int length = Math.min(positions.length, values.length);

        data.envData.ensureCapacity(length);

        for (int i = 0; i < length; i++) {
            data.envData.put(positions[i], values[i]);
        }

        return data;
    }

    public EnvironmentData getDataForPosition(BlockPos pos) {
        int packed = envData.getOrDefault(pos.asLong(), EnvironmentData.DEFAULT_PACKED);
        return EnvironmentData.unpack(packed);
    }

    public void setDataForPosition(BlockPos pos, EnvironmentData data) {
        putOrRemove(pos.asLong(), data.pack());
    }

    public boolean hasOxygen(BlockPos pos) {
        var data = getDataForPosition(pos);
        return data.hasOxygen();
    }

    public void setOxygen(BlockPos pos, boolean value) {
        var data = getDataForPosition(pos);
        data.setOxygen(value);
        putOrRemove(pos.asLong(), data.pack());
    }

    public float getGravity(BlockPos pos) {
        var data = getDataForPosition(pos);
        return data.getGravity();
    }

    public void setGravity(BlockPos pos, float value) {
        var data = getDataForPosition(pos);
        data.setGravity(value);
        putOrRemove(pos.asLong(), data.pack());
    }

    public short getTemperature(BlockPos pos) {
        var data = getDataForPosition(pos);
        return data.getTemperature();
    }

    public void setTemperature(BlockPos pos, short value) {
        var data = getDataForPosition(pos);
        data.setTemperature(value);
        putOrRemove(pos.asLong(), data.pack());
    }

    private void putOrRemove(long key, int packed) {
        if (packed == EnvironmentData.DEFAULT_PACKED) {
            envData.remove(key);
        } else {
            envData.put(key, packed);
        }
        setDirty();
    }

    public static EnvironmentSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(EnvironmentSavedData::create, EnvironmentSavedData::load),
                NAME
        );
    }
}
