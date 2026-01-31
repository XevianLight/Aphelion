package net.xevianlight.aphelion.block.entity.custom;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import static net.xevianlight.aphelion.Aphelion.LOGGER;

import net.minecraft.world.level.chunk.LevelChunk;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import net.xevianlight.aphelion.core.saveddata.EnvironmentSavedData;
import net.xevianlight.aphelion.util.FloodFill3D;
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

    public static final int MAX_RANGE = 16;
    public boolean isInRange(BlockPos pos1, BlockPos pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY()) + Math.abs(pos1.getZ() - pos2.getZ()) <= MAX_RANGE;
    }

    public void removeEnclosed() {
        if (enclosedCache != null) {
            var singleplayerServer = Minecraft.getInstance().getSingleplayerServer();
            if (singleplayerServer != null) {
                var serverLevel = singleplayerServer.getLevel(level.dimension());
                if (serverLevel != null) {
                    var savedData = EnvironmentSavedData.get(serverLevel);
                    savedData.resetOxygen(serverLevel, enclosedCache);
                    enclosedCache = Set.of();
                }
            }
        }
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
            int bitPos = inX & 15; // bottom 4 bits of X is bit pos
            int bit = (1 << bitPos);

            // First (bitsSize-4) bits are for X, next (bitsSize) bits are for Z, next (bitsSize) bits are for Y
            int arrayPos = (inX >>> 4) + (inZ << (bitsSize-4)) + (inY << (bitsSize*2-4));

            boolean out = !((_grid[arrayPos] & bit) > 0);
            _grid[arrayPos] = _grid[arrayPos] | bit;
            return out;
        }
    }

    private Set<BlockPos> enclosedCache;


    public Set<BlockPos> getEnclosedCache() {
        return enclosedCache;
    }

    public Set<BlockPos> getEnclosedBlocks() {
        if (level == null) return Set.of();
//        if (enclosedCache != null) return enclosedCache;

        long start = System.nanoTime();
        Set<BlockPos> enclosedBlocks = FloodFill3D.run(level, getBlockPos(), 6000, FloodFill3D.TEST_FULL_SEAL, true);

//        // make this bitch BIIIIIG
//        BigBoolGrid seen = new BigBoolGrid(8, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ());
//
//        // It's... a reasonable assumption that we won't have to include more blocks at once than the area of a sphere?
//        // maybe a bit more, IDK how exactly it scales to blocks.
//        Stack<BlockPos> stack = new Stack<>();
//        Stack<Integer> radiusStack = new Stack<>();
//
//        stack.add(this.getBlockPos());

        // Do flood fill out from this block
        // Push on the top of the stack (newest), pop from the bottom of the stack (oldest).
        // Ends up being breadth-first; if you wanted, you could label each position pushed to the queue
        // by how many "layers deep" it is (i.e. how many steps it took to get there),
        // and you'd see that every pos of layer 1 is together, then layer 2, then layer 3...


//        BlockPos ourPos = getBlockPos();
//        while (!stack.isEmpty()) {
//            BlockPos spreadFromPos = stack.pop();
//            for (Direction d : Direction.values()) {
//                BlockPos relativePos = spreadFromPos.relative(d);
//
//                if (isInRange(relativePos, ourPos) && canSpreadTo(relativePos)) {
//                    // seen.add runs seen.contains under the hood,
//                    // + this is the most expensive operation.
//                    // should save a lot of time!
//                    if (seen.add(relativePos.getX(), relativePos.getY(), relativePos.getZ())) {
//                        enclosedBlocks.add(relativePos);
//                        stack.add(relativePos);
//                    }
//                }
//            }
//        }

        var singleplayerServer = Minecraft.getInstance().getSingleplayerServer();
        if (singleplayerServer != null) {
            var serverLevel = singleplayerServer.getLevel(level.dimension());
            if (serverLevel != null) {

                // Build a set of longs for the newly computed blocks (order-independent)
                boolean changed = isChanged(enclosedBlocks);
//                boolean changed = false;
                if (changed) {
                    var savedData = EnvironmentSavedData.get(serverLevel);

                    // Revert old affected area back to defaults
                    if (enclosedCache != null) {
                        savedData.resetOxygen(serverLevel, enclosedCache);
                    }

                    // Apply oxygen to new affected area
                    savedData.setOxygen(serverLevel, enclosedBlocks, true);

                    LOGGER.info("Saved data for {} blocks to leveldata", enclosedBlocks.size());
                }

                // Update the cache no matter what (so next compare is correct)
                enclosedCache = enclosedBlocks;
            }
        }
        long durationNanos = System.nanoTime() - start;
        double durationMicros = durationNanos / 1000.0d;
        LOGGER.info("Flood fill completed in {}µs, {} blocks at {}µs/block", durationMicros, enclosedBlocks.size(), durationMicros / enclosedBlocks.size());

        enclosedCache = enclosedBlocks;

        return enclosedCache;
    }

    private boolean isChanged(Set<BlockPos> enclosedBlocks) {
        LongOpenHashSet newSet = new LongOpenHashSet(enclosedBlocks.size());
        for (BlockPos p : enclosedBlocks) {
            newSet.add(p.asLong());
        }

        // Build a set of longs for the cached blocks (if any)
        LongOpenHashSet oldSet = null;
        if (enclosedCache != null) {
            oldSet = new LongOpenHashSet(enclosedCache.size());
            for (BlockPos p : enclosedCache) {
                oldSet.add(p.asLong());
            }
        }

        // Only save if the set of affected blocks has changed
        boolean changed = (oldSet == null) || !oldSet.equals(newSet);
        return changed;
    }

    private void helper() {
        var myVar = new BlockPos(1, 1, 1).hashCode();
    }

    int ticks = 0;
    int refreshAfter = 20;



    public void tick(Level level, BlockPos pos, BlockState blockState) {
        if (level.isClientSide) return;
        ticks++;
        if (ticks >= refreshAfter) {
            getEnclosedBlocks();
            ticks = 0;

            // UNCOMMENT FOR DEBUG ONLY!!! EXTREMELY TPS INTENSIVE!!!
//            EnvironmentSavedData.refreshFromIntegratedServerIfNeeded(Minecraft.getInstance(), 64, 10000);
        }
    }

}
