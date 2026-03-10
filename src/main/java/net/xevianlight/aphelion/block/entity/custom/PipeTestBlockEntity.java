package net.xevianlight.aphelion.block.entity.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.custom.PipeTestBlock;
import net.xevianlight.aphelion.block.custom.base.TickableBlockEntity;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import net.xevianlight.aphelion.core.init.ModBlocks;
import net.xevianlight.aphelion.systems.conveyor.*;
import net.xevianlight.aphelion.util.FloodFill3D;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PipeTestBlockEntity extends BlockEntity implements TickableBlockEntity {
    public @Nullable ConveyorNetwork graph = null;
    public final Map<Direction, @Nullable ConveyorAttachment> attachments = new HashMap<>();
    public final Map<Direction, @Nullable ConveyorOutput> outputs = new HashMap<>();

    public PipeTestBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PIPE_TEST_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public void clientTick(ClientLevel level, long time, BlockState state, BlockPos pos) {

    }

    @Override
    public void serverTick(ServerLevel level, long time, BlockState state, BlockPos pos) {
        if (this.graph == null) initGraph(level, pos);
        // TODO: Call this as little as necessary
        makeOutputs(level, state, pos);

        for (Direction dir : Direction.values()) {
            ConveyorAttachment attachment = attachments.get(dir);
            if (attachment != null) {
                attachment.tick(level, state, pos, dir, graph);
            }
        }
    }

    @Override
    public void firstTick(Level level, BlockState state, BlockPos pos) {

    }

    private void addOutput(Direction dir, ConveyorOutput output) {
        outputs.put(dir, output);
        if (graph != null) graph.outputs.add(output);
    }

    private void removeOutput(Direction dir) {
        ConveyorOutput old = outputs.get(dir);
        if (graph != null) graph.outputs.remove(old);
        outputs.remove(dir);
    }

    private boolean canOutputTo(Level level, BlockPos pos, Direction accessSide) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, accessSide) != null;
    }

    protected void makeOutputs(ServerLevel level, BlockState state, BlockPos pos) {
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            neighbor.setWithOffset(pos, dir);

            if (canOutputTo(level, neighbor, dir.getOpposite()) && attachments.get(dir) == null && outputs.get(dir) == null) {
                addOutput(dir, new BasicItemOutput(level, pos, dir));
            }

            if (!(canOutputTo(level, neighbor, dir.getOpposite()) && attachments.get(dir) == null) && outputs.get(dir) != null) {
                removeOutput(dir);
            }
        }
    }

    public boolean hasAttachment(Direction direction) {
        return attachments.get(direction) != null;
    }

    // Simplest implementation I can think of
    public static void initGraph(Level level, BlockPos pos) {
        Aphelion.LOGGER.info("Init graph from {}", pos);
        if (!level.getBlockState(pos).is(ModBlocks.PIPE_TEST_BLOCK.get())) return;
        Set<BlockPos> pipes = FloodFill3D.run(
                level, pos, 1000,
                (var v1, var v2, var state, var v4, var v5, var v6) -> state.is(ModBlocks.PIPE_TEST_BLOCK.get()),
                false);

        Aphelion.LOGGER.info("Got {} pipes", pipes.size());
        ConveyorNetwork graph = new ConveyorNetwork();
        for (BlockPos pipePos : pipes) {
            BlockEntity BE = level.getBlockEntity(pipePos);
            if (BE instanceof PipeTestBlockEntity pipe) {
                // Invalidate any old graphs
                if (pipe.graph != null) {
                    pipe.graph.invalidate();
                }

                graph.addPipe(pipe);
            }
        }
    }

    public void setAttachment(Direction side, ConveyorAttachment attachment) {
        attachments.put(side, attachment);
    }

    public boolean trySetAttachment(Direction side, ConveyorAttachment attachment) {
        if (hasAttachment(side)) return false;
        setAttachment(side, attachment);
        return true;
    }

    public static boolean isAttachmentItem(ItemStack stack) {
        //TODO: add actual attachment items instead of just stone
        return stack.is(Item.byId(1));
    }

    // Server only
    public ConveyorAttachment getAttachmentForItem(ItemStack stack, Direction side) {
        if (level.isClientSide()) throw new RuntimeException("Cannot get attachment item on client side!");
        return new BasicItemExtractAttachment((ServerLevel) level, getBlockPos(), side);
    }

    /// Called from pipe test block's useItemOn
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, PipeTestBlock block) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        Vec3 relativePos = hitResult.getLocation().subtract(pos.getCenter());
        Direction pipeSide = Direction.getNearest(relativePos);

        if (isAttachmentItem(stack)) {
            boolean success = trySetAttachment(pipeSide, getAttachmentForItem(stack, pipeSide));

            return success ? ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
        } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; // goes through with whatever other interaction (placing a block, etc)
        }
    }
}
