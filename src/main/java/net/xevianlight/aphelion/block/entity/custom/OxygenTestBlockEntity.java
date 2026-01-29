package net.xevianlight.aphelion.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import static net.xevianlight.aphelion.Aphelion.LOGGER;
import net.xevianlight.aphelion.core.init.ModBlockEntities;

import java.util.*;

public class OxygenTestBlockEntity extends BlockEntity {


    public OxygenTestBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.OXYGEN_TEST_BLOCK_ENTITY.get(), pos, blockState);
    }

    public boolean canSpreadTo(BlockPos pos) {
        return level != null && level.getBlockState(pos).isAir();
    }

    public static final int MAX_RANGE = 8;
    public boolean isInRange(BlockPos pos, int radius) {
        return radius <= MAX_RANGE;
    }

    private List<BlockPos> enclosedCache;
    public List<BlockPos> getEnclosedBlocks() {
        if (level == null) return List.of();
        if (enclosedCache != null) return enclosedCache;

        List<BlockPos> enclosedBlocks = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();

        // It's... a reasonable assumption that we won't have to include more blocks at once than the area of a sphere?
        // maybe a bit more, IDK how exactly it scales to blocks.
        Deque<BlockPos> queue = new ArrayDeque<>((int)(4 * Math.PI * MAX_RANGE * MAX_RANGE));
        Deque<Integer> radiusQueue = new ArrayDeque<>((int)(4 * Math.PI * MAX_RANGE * MAX_RANGE));

        queue.add(this.getBlockPos());
        radiusQueue.add(0);

        // Do flood fill out from this block
        // Push on the top of the stack (newest), pop from the bottom of the stack (oldest).
        // Ends up being breadth-first; if you wanted, you could label each position pushed to the queue
        // by how many "layers deep" it is (i.e. how many steps it took to get there),
        // and you'd see that every pos of layer 1 is together, then layer 2, then layer 3...

        long start = System.nanoTime();
        while (!queue.isEmpty()) {
            BlockPos spreadFromPos = queue.removeFirst();
            int radius = radiusQueue.removeFirst();
            for (Direction d : Direction.values()) {
                BlockPos relativePos = spreadFromPos.relative(d);
                if (seen.contains(relativePos)) continue;

                if (canSpreadTo(relativePos) && isInRange(relativePos, radius)) {
                    seen.add(relativePos);
                    enclosedBlocks.add(relativePos);

                    queue.add(relativePos);
                    radiusQueue.add(radius + 1);
                }
            }
        }
        long durationNanos = System.nanoTime() - start;
        double durationMicros = durationNanos / 1000.0d;
        LOGGER.info("Flood fill completed in {}µs, {}µs/block", durationMicros, durationMicros / enclosedBlocks.size());

        enclosedCache = enclosedBlocks;
        return enclosedBlocks;
    }

    private void helper() {
        var myVar = new BlockPos(1, 1, 1).hashCode();
    }


}
