package net.xevianlight.aphelion.block.entity.custom;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import static net.xevianlight.aphelion.Aphelion.LOGGER;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import org.openjdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

import java.util.*;

public class OxygenTestBlockEntity extends BlockEntity {


    public OxygenTestBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.OXYGEN_TEST_BLOCK_ENTITY.get(), pos, blockState);
    }

    private LevelChunk lastChunk;
    private int lastChunkX = Integer.MIN_VALUE, lastChunkZ = Integer.MIN_VALUE;

    public BlockState fastBlockState(BlockPos pos) {
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        if (cx != lastChunkX || cz != lastChunkZ) {
            lastChunk = level.getChunk(cx, cz);

            lastChunkX = cx;
            lastChunkZ = cz;
        }
        return lastChunk.getBlockState(pos);
    }

    public boolean canSpreadTo(BlockPos pos) {
        return level != null && fastBlockState(pos).isAir();
    }

    public static final int MAX_RANGE = 100;
    public boolean isInRange(BlockPos pos1, BlockPos pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY()) + Math.abs(pos1.getZ() - pos2.getZ()) <= MAX_RANGE;
    }

    /// 256*256*256 grid of booleans
    private class BigBoolGrid {
        int bitsSize;
        int size;
        private int[] _grid;
        int xOff, yOff, zOff;

        /// Note that the ACTUAL size is 2^bitsSize, so 128 for example would be 7
        /// max bitsSize is 12(?)
        BigBoolGrid (int bitsSize, int xOrigin, int yOrigin, int zOrigin) {
            if (Integer.bitCount(bitsSize) != 1) throw new ValueException("Grid size " + bitsSize + " is not a power of two!");

            this.bitsSize = bitsSize;
            this.size = (1 << bitsSize);
            if (this.size < 8) throw new ValueException("Grid size is too small!");

            _grid = new int[size * size * size / 8];

            xOff = -xOrigin;
            yOff = -yOrigin;
            zOff = -zOrigin;
        }

        public int getArrayPos(int x, int y, int z) {
            return 0;
        }

        public int getBit(int x) {
            return 0;
        }

        // Returns the previous value at this location
        public boolean add(int x, int y, int z) {
            final int inX, inY, inZ;
            inX = x + xOff + size / 2;
            inY = y + yOff + size / 2;
            inZ = z + zOff + size / 2;
            if (inX >= size || inX < 0) throw new ValueException("Coordinate X out of range");
            if (inY >= size || inY < 0) throw new ValueException("Coordinate Y out of range");
            if (inZ >= size || inZ < 0) throw new ValueException("Coordinate Z out of range");

            // the bytes are really just packed groups of 8 booleans,
            // "laid across" the X direction.
            // wrapping order is X->Z->Y, so we go along the X axis,
            // wrap to the Z axis to make a square, and once the square is full
            // we step up once along the Y axis.
            int bitPos = inX & 7; // bottom 4 bits of X is bit pos
            int bit = (1 << bitPos);

            // First (bitsSize-4) bits are for X, next (bitsSize) bits are for Z, next (bitsSize) bits are for Y
            int arrayPos = (inX >>> 4) + (inZ << (bitsSize-4)) + (inY << (bitsSize*2-4));

            boolean out = !((_grid[arrayPos] & bit) > 0);
            _grid[arrayPos] = _grid[arrayPos] | bit;
            return out;
        }
    }

    private List<BlockPos> enclosedCache;
    public List<BlockPos> getEnclosedBlocks() {
        if (level == null) return List.of();
        if (enclosedCache != null) return enclosedCache;

        long start = System.nanoTime();
        List<BlockPos> enclosedBlocks = new ArrayList<>();
        // make this bitch BIIIIIG
        BigBoolGrid seen = new BigBoolGrid(8, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ());

        // It's... a reasonable assumption that we won't have to include more blocks at once than the area of a sphere?
        // maybe a bit more, IDK how exactly it scales to blocks.
        Stack<BlockPos> stack = new Stack<>();
        Stack<Integer> radiusStack = new Stack<>();

        stack.add(this.getBlockPos());

        // Do flood fill out from this block
        // Push on the top of the stack (newest), pop from the bottom of the stack (oldest).
        // Ends up being breadth-first; if you wanted, you could label each position pushed to the queue
        // by how many "layers deep" it is (i.e. how many steps it took to get there),
        // and you'd see that every pos of layer 1 is together, then layer 2, then layer 3...


        BlockPos ourPos = getBlockPos();
        while (!stack.isEmpty()) {
            BlockPos spreadFromPos = stack.pop();
            for (Direction d : Direction.values()) {
                BlockPos relativePos = spreadFromPos.relative(d);

                if (isInRange(relativePos, ourPos) && canSpreadTo(relativePos)) {
                    // seen.add runs seen.contains under the hood,
                    // + this is the most expensive operation.
                    // should save a lot of time!
                    if (seen.add(relativePos.getX(), relativePos.getY(), relativePos.getZ())) {
                        enclosedBlocks.add(relativePos);
                        stack.add(relativePos);
                    }
                }
            }
        }
        long durationNanos = System.nanoTime() - start;
        double durationMicros = durationNanos / 1000.0d;
        LOGGER.info("Flood fill completed in {}µs, {} blocks at {}µs/block", durationMicros, enclosedBlocks.size(), durationMicros / enclosedBlocks.size());

        enclosedCache = enclosedBlocks;
        return enclosedBlocks;
    }

    private void helper() {
        var myVar = new BlockPos(1, 1, 1).hashCode();
    }


}
