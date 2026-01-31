package net.xevianlight.aphelion.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Standalone flood-fill utility.
 * - Traverses blocks starting from origin
 * - Visits only positions allowed by Passable predicate
 * - Bounded by maxRange (Manhattan distance)
 * - Returns all visited positions (excluding origin by default)
 */
public final class TechnoFloodFill {

    private TechnoFloodFill() {}

    @FunctionalInterface
    public interface Passable {
        boolean test(Level level, BlockPos pos, BlockState state);
    }

    /** Convenience predicate: treat air as passable. */
    public static final Passable AIR_ONLY = (level, pos, state) -> state.isAir();

    /**
     * Runs a bounded flood fill.
     *
     * @param level the world
     * @param origin starting position (typically BE position)
     * @param maxRange Manhattan range limit (|dx|+|dy|+|dz|)
     * @param passable which blocks can be entered/added
     * @param includeOrigin whether to include origin in the returned list
     */
    public static List<BlockPos> run(Level level, BlockPos origin, int maxRange, Passable passable, boolean includeOrigin) {
        if (level == null) return List.of();

        // Choose a grid size big enough to cover maxRange in all axes.
        // We need coordinates within [-maxRange, +maxRange] around origin.
        // So size must be >= (2*maxRange + 1). Next power-of-two for cheap indexing.
        int needed = 2 * maxRange + 1;
        int sizePow2 = nextPow2(needed);
        int bits = Integer.numberOfTrailingZeros(sizePow2); // since pow2
        BigVisitedGrid seen = new BigVisitedGrid(bits, origin.getX(), origin.getY(), origin.getZ());

        // Chunk-cached blockstate fetch (no BE state needed)
        ChunkCache chunkCache = new ChunkCache(level);

        List<BlockPos> out = new ArrayList<>();
        Deque<BlockPos> stack = new ArrayDeque<>();

        if (includeOrigin) {
            out.add(origin);
        }

        // Mark origin visited so we don't bounce back into it.
        seen.add(origin.getX(), origin.getY(), origin.getZ());
        stack.push(origin);

        while (!stack.isEmpty()) {
            BlockPos from = stack.pop();

            for (Direction d : Direction.values()) {
                BlockPos next = from.relative(d);

                if (!inRangeManhattan(origin, next, maxRange)) continue;

                // visited check first: cheapest early-out
                if (!seen.add(next.getX(), next.getY(), next.getZ())) continue;

                BlockState st = chunkCache.getBlockState(next);
                if (!passable.test(level, next, st)) continue;

                out.add(next);
                stack.push(next);
            }
        }

        return out;
    }

    private static boolean inRangeManhattan(BlockPos a, BlockPos b, int max) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int dz = Math.abs(a.getZ() - b.getZ());
        return dx + dy + dz <= max;
    }

    private static int nextPow2(int x) {
        // smallest power of 2 >= x, with a minimum of 8
        int v = 1;
        while (v < x) v <<= 1;
        return Math.max(v, 8);
    }

    /**
     * Simple chunk cache for repeated reads in flood fill.
     */
    private static final class ChunkCache {
        private final Level level;
        private LevelChunk lastChunk;
        private int lastCx = Integer.MIN_VALUE;
        private int lastCz = Integer.MIN_VALUE;

        private ChunkCache(Level level) {
            this.level = level;
        }

        BlockState getBlockState(BlockPos pos) {
            int cx = pos.getX() >> 4;
            int cz = pos.getZ() >> 4;

            if (cx != lastCx || cz != lastCz || lastChunk == null) {
                lastChunk = level.getChunk(cx, cz); // may load/generate; okay for your current usage
                lastCx = cx;
                lastCz = cz;
            }
            return lastChunk.getBlockState(pos);
        }
    }

    /**
     * Packed visited grid centered on an origin.
     *
     * Uses an int[] with 32 bits per word:
     * - size = 2^bits (must be power-of-two)
     * - Word index layout: ((y * size) + z) * wordsPerRow + (x / 32)
     * - Bit inside the word: (x % 32)
     */
    private static final class BigVisitedGrid {
        private final int bits;
        private final int size;
        private final int wordsPerRow;
        private final int[] words;
        private final int xOff, yOff, zOff;

        BigVisitedGrid(int bits, int xOrigin, int yOrigin, int zOrigin) {
            if (bits < 3) throw new IllegalArgumentException("Grid too small (bits=" + bits + ")");
            if (bits > 12) throw new IllegalArgumentException("Grid too large (bits=" + bits + ")");

            this.bits = bits;
            this.size = 1 << bits;

            // Center origin at middle of grid
            this.xOff = -xOrigin + (size / 2);
            this.yOff = -yOrigin + (size / 2);
            this.zOff = -zOrigin + (size / 2);

            if ((size & 31) != 0) {
                // to keep wordsPerRow integer
                throw new IllegalArgumentException("Grid size must be divisible by 32, got " + size);
            }

            this.wordsPerRow = size >>> 5; // size / 32
            int totalWords = wordsPerRow * size * size; // (y,z) rows * x-words
            this.words = new int[totalWords];
        }

        /**
         * @return true if it was NOT previously visited and is now marked visited
         */
        boolean add(int x, int y, int z) {
            int inX = x + xOff;
            int inY = y + yOff;
            int inZ = z + zOff;

            // Bounds check (fast and safe)
            if ((inX | inY | inZ) < 0 || inX >= size || inY >= size || inZ >= size) {
                return false; // out of grid => treat as "already seen" to prevent expansion
            }

            int wordIndex = ((inY * size) + inZ) * wordsPerRow + (inX >>> 5);
            int bit = 1 << (inX & 31);

            int prev = words[wordIndex];
            if ((prev & bit) != 0) return false;

            words[wordIndex] = prev | bit;
            return true;
        }
    }
}
