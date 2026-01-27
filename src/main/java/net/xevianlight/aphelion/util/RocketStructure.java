package net.xevianlight.aphelion.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public final class RocketStructure {


    private final List<BlockState> palette = new ArrayList<>();
    private final IntList packedPositions = new IntArrayList();
    private final IntList paletteIndices = new IntArrayList();

    public RocketStructure(Builder builder) {
        builder.build(this);
    }

    @FunctionalInterface
    public interface Builder {
        void build(RocketStructure s);
    }

    public int size() { return packedPositions.size(); }

    public void clear() {
        palette.clear();
        packedPositions.clear();
        paletteIndices.clear();
    }

    public void add(int x, int y, int z, BlockState state) {
        if (state == Blocks.AIR.defaultBlockState()) return;
        int idx = palette.indexOf(state);
        if (idx < 0) {
            palette.add(state);
            idx = palette.size() - 1;
        }

        packedPositions.add(packPos(x, y, z));
        paletteIndices.add(idx); // ← REQUIRED
    }


    public BlockState stateAt(int i) { return palette.get(paletteIndices.getInt(i)); }
    public int packedPosAt(int i) { return packedPositions.getInt(i); }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // palette
        ListTag pal = new ListTag();
        for (BlockState st : palette) {
            pal.add(BlockState.CODEC.encodeStart(NbtOps.INSTANCE, st)
                    .getOrThrow());
        }
        tag.put("palette", pal);

        // blocks
        IntArrayTag posArr = new IntArrayTag(packedPositions.toIntArray());
        IntArrayTag idxArr = new IntArrayTag(paletteIndices.toIntArray());
        tag.put("pos", posArr);
        tag.put("idx", idxArr);

        return tag;
    }

    public void load(CompoundTag tag) {
        clear();

        // palette
        ListTag pal = tag.getList("palette", Tag.TAG_COMPOUND);
        for (int i = 0; i < pal.size(); i++) {
            Tag stTag = pal.get(i);
            BlockState st = BlockState.CODEC.parse(NbtOps.INSTANCE, stTag)
                    .getOrThrow();
            palette.add(st);
        }

        // blocks
        int[] pos = tag.getIntArray("pos");
        int[] idx = tag.getIntArray("idx");

        for (int i = 0; i < pos.length; i++) {
            packedPositions.add(pos[i]);
            paletteIndices.add(idx[i]);
        }
    }

    public static int packPos (int x, int y, int z) {
        return (x & 0xFF) | ((y & 0xFF) << 8) | ((z & 0xFF) << 16);
    }

    public static int unpackX (int p) { return (byte) (p & 0xFF); }
    public static int unpackY (int p) { return (byte) (( p >> 8) & 0xFF); }
    public static int unpackZ (int p) { return (byte) (( p >> 16) & 0xFF); }

    public record Extents(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public int sizeX() { return maxX - minX + 1; }
        public int sizeY() { return maxY - minY + 1; }
        public int sizeZ() { return maxZ - minZ + 1; }

        public AABB toLocalAABB() {
            return new AABB(minX - 0.5, minY, minZ - 0.5, maxX + 0.5, maxY + 1, maxZ + 0.5);
        }
    }

    public Extents computeExtents() {
        if (size() == 0) {
            return new Extents(0, 0, 0, 0, 0, 0);
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (int i = 0; i < size(); i++) {
            int p = packedPosAt(i);
            int x = unpackX(p);
            int y = unpackY(p);
            int z = unpackZ(p);

            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;

            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
        }

        return new Extents(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static RocketStructure capture(Level level, BlockPos origin, int rx, int ry, int rz) {
        return new RocketStructure(s -> {
            for (int dy = -ry; dy <= ry; dy++) {
                for (int dx = -rx; dx <= rx; dx++) {
                    for (int dz = -rz; dz <= rz; dz++) {
                        BlockPos p = origin.offset(dx, dy, dz);
                        BlockState st = level.getBlockState(p);

                        // Skip air and unbreakables/forbidden blocks as you like
                        if (st.isAir()) continue;

                        // Optional: ignore the assembler block itself
                        // if (p.equals(origin)) continue;

                        s.add(dx, dy, dz, st);
                    }
                }
            }
        });
    }

    public static void clearCaptured(Level level, BlockPos origin, RocketStructure struct) {
        for (int i = 0; i < struct.size(); i++) {
            int packed = struct.packedPosAt(i);
            int dx = RocketStructure.unpackX(packed);
            int dy = RocketStructure.unpackY(packed);
            int dz = RocketStructure.unpackZ(packed);

            BlockPos wp = origin.offset(dx, dy, dz);
            level.setBlock(wp, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
