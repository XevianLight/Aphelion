package net.xevianlight.aphelion.block.entity.custom;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.custom.base.TickableBlockEntity;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import net.xevianlight.aphelion.core.init.ModBlocks;
import net.xevianlight.aphelion.entites.vehicles.RocketEntity;
import net.xevianlight.aphelion.util.AphelionBlockStateProperties;
import net.xevianlight.aphelion.util.ModTags;
import net.xevianlight.aphelion.util.RocketStructure;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;

public class RocketAssemblerBlockEntity extends BlockEntity implements TickableBlockEntity {

    Direction facing;
    BlockPos padScanStart = BlockPos.ZERO;
    private PadInfo padBounds;
    RocketEntity lastRocket;

    public @Nullable PadInfo getPadBounds() {
        return padBounds;
    }

    public boolean isInitialized;
    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    public RocketAssemblerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ROCKET_ASSEMBLER_BLOCK_ENTITY.get(), pos, blockState);
    }

    public record PadInfo(BlockPos min, BlockPos max) {
        public int getVolume() {
            int dx = max.getX() - min.getX() + 1;
            int dy = max.getY() - min.getY() + 1;
            int dz = max.getZ() - min.getZ() + 1;

            return dx * dy * dz;
        }
    }

    private final Block TOWER_BLOCK = ModBlocks.BLOCK_STEEL.get();
    public BlockPos towerBasePos;

    private boolean connected(BlockState state, Direction dir) {
        return switch (dir) {
            case NORTH -> state.getValue(BlockStateProperties.NORTH);
            case SOUTH -> state.getValue(BlockStateProperties.SOUTH);
            case EAST -> state.getValue(BlockStateProperties.EAST);
            case WEST -> state.getValue(BlockStateProperties.WEST);
            default -> false;
        };
    }

    public @Nullable PadInfo getPlatformViaFill() {
        if (level == null) return null;

        BlockPos start = this.padScanStart;
        if (!isPad(level.getBlockState(start))) return null;


        final int y = start.getY();
        int towerHeight = 0;

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        LongOpenHashSet visited = new LongOpenHashSet();

        queue.add(start);
        visited.add(start.asLong());

        final Direction[] CARDINALS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        // When the $#$# are we going to have a rocket larger than 32x32... don't...
        final int MAX_PAD_BLOCKS = 1024;

        boolean towerFound = false;
        while (!queue.isEmpty()) {
            BlockPos p = queue.removeFirst();
            BlockState s = level.getBlockState(p);

            // We trust block states entirely. If a block state says it has a pad in a direction and is wrong, something else has failed. To minimize level checks and have this run as fast as possible, I won't compare the block
//            if (!isPad(s)) continue;

            for (Direction d : CARDINALS) {
                // Only keep going if the block claims to have a neighbor in that direction
                BlockPos n = p.relative(d);

                if (!connected(s, d)) {
                    if (isTower(level.getBlockState(n))) {
                        if (!towerFound) {
                            towerBasePos = n;
                            towerFound = true;
                        } else if (!n.equals(towerBasePos)) {
                            Aphelion.LOGGER.warn("Multiple towers found, rocket pad invalid");
                            return null;
                        }
                    }
                    continue;
                }

                if (visited.contains(n.asLong())) continue; // skip if we've already seen this block

                visited.add(n.asLong());
                queue.addLast(n);

                if (visited.size() > MAX_PAD_BLOCKS) return null;

            }
        }

        // Pads missing a tower should be invalid
        if (!towerFound || towerBasePos == null) return null;
        towerHeight = getTowerHeight(level, towerBasePos);
        if (towerHeight <= 0) return null;

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (Long p : visited) {
            minX = Math.min(minX, BlockPos.of(p).getX());
            maxX = Math.max(maxX, BlockPos.of(p).getX());
            minZ = Math.min(minZ, BlockPos.of(p).getZ());
            maxZ = Math.max(maxZ, BlockPos.of(p).getZ());
        }

        int width = (maxX - minX) + 1;
        int length = (maxZ - minZ) + 1;

        // SQUARE AND SOLID??? The math works out here that this will only be true if the number of blocks found matches the side lengths squared. We don't need to check for holes manually!
        if (visited.size() != length * width || length != width) return null;

        return new PadInfo(new BlockPos(minX, y + 1, minZ), new BlockPos(maxX, y + towerHeight, maxZ));
    }

    private int getTowerHeight(@NotNull Level level, @NotNull BlockPos base) {
        int h = 0;
        BlockPos p = base.above();

        while (isTower(level.getBlockState(p))) {
            h++;
            p = p.above();
        }
        return h;
    }

    private BlockPos seatPos;

    public @Nullable RocketStructure scan() {
        if (level == null) return null;

        seatPos = null;

        PadInfo bounds = padBounds;
        if (bounds == null) return null;


        BlockPos min = bounds.min();
        BlockPos max = bounds.max();

        // Find seat, every rocket must have a seat and all blocks in the rocket must be attached to it
        for (int y = min.getY(); y <= max.getY(); y++) {
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState st = level.getBlockState(p);

                    if (!st.is(ModTags.Blocks.ROCKET_SEAT)) continue;

                    if (seatPos != null && !seatPos.equals(p)) {
                        Aphelion.LOGGER.warn("Rocket scan failed: multiple seats found");
                        seatPos = null;
                        return null;
                    }
                    seatPos = p;
                }
            }
        }

        if (seatPos == null) {
            Aphelion.LOGGER.warn("Rocket scan failed: no seat found");
            seatPos = null;
            return null;
        }

        final Direction[] DIRS = Direction.values();

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        LongOpenHashSet visited = new LongOpenHashSet();

        queue.add(seatPos);
        visited.add(seatPos.asLong());

        RocketStructure structure = new RocketStructure(s -> {});
        structure.addSeatOffset(0,0,0);

        final int MAX_ROCKET_BLOCKS = 1000;

        while (!queue.isEmpty()) {
            BlockPos p = queue.removeFirst();
            BlockState st = level.getBlockState(p);

            if (!within(bounds, p)) continue;

            // Any block cases we should IGNORE
            if (st.isAir()) continue; // ignore air
            if (isPad(st)) continue; // ignore the pad
            if (isTower(st)) continue; // ignore the tower
            if (p.equals(this.worldPosition)) continue; // ignore the assembler

            // Reject block entities
            if (st.hasBlockEntity() || level.getBlockEntity(p) != null) {
                Aphelion.LOGGER.warn("Rocket scan failed: found block entity at {}", p);
                return null;
            }

            int dx = p.getX() - seatPos.getX();
            int dy = p.getY() - seatPos.getY();
            int dz = p.getZ() - seatPos.getZ();

            if (!fitsSignedByte(dx) || !fitsSignedByte(dy) || !fitsSignedByte(dz)) {
                Aphelion.LOGGER.warn("Rocket scan failed: structure too large to pack at {} (dx={},dy={},dz={})", p, dx, dy, dz);
                return null;
            }

            // All checks succeeded, add the block to the rocket
            structure.add(dx, dy, dz, st);

            // Explore neighbors
            for (Direction d : DIRS) {
                BlockPos n = p.relative(d);

                if (!within(bounds, n)) continue; // Skip neighbor outside of rocket assembler bounds

                long key = n.asLong();
                if (visited.contains(key)) continue; // Skip visited blocks

                BlockState ns = level.getBlockState(n);

                if (ns.isAir()) continue;
                if (isPad(ns)) continue;
                if (isTower(ns)) continue;
                if (n.equals(this.worldPosition)) continue;

                visited.add(key);
                queue.addLast(n);

                if (visited.size() >= MAX_ROCKET_BLOCKS) {
                    Aphelion.LOGGER.warn("Rocket scan failed: exceeded max blocks ({})", MAX_ROCKET_BLOCKS);
                    return null;
                }
            }
        }

        if (structure.size() == 0) return null;
        return structure;
    }

    public @Nullable RocketEntity assemble() {
        if (level == null) return null;
        RocketStructure structure = scan();
        RocketEntity rocket = null;
        if (lastRocket != null)
            lastRocket.disassemble();
        if (structure != null && seatPos != null) {
            RocketStructure.clearCaptured(level, seatPos, structure);
            rocket = RocketEntity.spawnRocket(level, seatPos, structure);
            Aphelion.LOGGER.info("Spawn rocket result: {}", rocket);
        }
        lastRocket = rocket;
        return rocket;
    }

    private static boolean within(PadInfo pad, BlockPos p) {
        BlockPos min = pad.min;
        BlockPos max = pad.max;

        return p.getX() >= min.getX() && p.getX() <= max.getX()
            && p.getY() >= min.getY() && p.getY() <= max.getY()
            && p.getZ() >= min.getZ() && p.getZ() <= max.getZ();
    }

    private static boolean fitsSignedByte(int v) {
        return v >= -128 && v <= 127;
    }

    @Override
    public void clientTick(ClientLevel level, long time, BlockState state, BlockPos pos) {

    }

    @Override
    public void serverTick(ServerLevel level, long time, BlockState state, BlockPos pos) {
        PadInfo newBounds = getPlatformViaFill();
        setPadBoundsAndSync(newBounds);

        boolean formed = newBounds != null;
        if (state.getValue(AphelionBlockStateProperties.FORMED) != formed) {
            level.setBlockAndUpdate(pos, state.setValue(AphelionBlockStateProperties.FORMED, formed));
        }
    }

    @Override
    public void firstTick(Level level, BlockState state, BlockPos pos) {
        facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        padScanStart = getBlockPos().mutable().below().relative(facing.getOpposite());
        this.isInitialized = true;
    }


    private static boolean isPad(BlockState s) {
        return s.is(ModTags.Blocks.LAUNCH_PAD); // or s.getBlock() == ModBlocks.PAD.get()
    }

    private static boolean isTower(BlockState s) {
        return s.is(ModBlocks.BLOCK_STEEL);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);

        PadInfo pad = this.padBounds;
        if (pad != null) {
            tag.putLong("PadMin", padBounds.min().asLong());
            tag.putLong("PadMax", padBounds.max().asLong());
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("PadMin") && tag.contains("PadMax")) {
            BlockPos min = BlockPos.of(tag.getLong("PadMin"));
            BlockPos max = BlockPos.of(tag.getLong("PadMax"));
            this.padBounds = new PadInfo(min, max);
        } else {
            this.padBounds = null;
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        loadAdditional(tag, registries);
    }

    private void setPadBoundsAndSync(@Nullable PadInfo newBounds) {
        if (java.util.Objects.equals(this.padBounds, newBounds)) return;

        this.padBounds = newBounds;
        setChanged(); // marks BE dirty for saving

        if (level instanceof ServerLevel server) {
            BlockState state = getBlockState();
            server.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // sends the tag from getUpdateTag()
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(@NotNull Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider registries) {
        // apply the received tag on client
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.loadAdditional(tag, registries);
        }
    }

    
}
