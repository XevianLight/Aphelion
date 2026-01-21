package net.xevianlight.aphelion.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.core.init.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MultiblockHelper {

    public record ShapePart (BlockPos offset, Predicate<BlockState> rule) {}

    public static List<BlockPos> getPartPositions(BlockState state, BlockPos pos, ShapePart[] shape) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        List<BlockPos> out = new ArrayList<>(shape.length);

        for (ShapePart part : shape) {
            BlockPos rotate = rotateY(part.offset, facing);
            out.add(pos.offset(rotate));
        }

        return out;
    }

    private static BlockPos rotateY(BlockPos offset, Direction facing) {
        // Assumes default shape faces north

        return switch (facing) {
            case NORTH  -> offset;
            case EAST   -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());
            case SOUTH  -> new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
            case WEST   -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());
            default     -> offset;
        };
    }

    private static BlockState effectiveState(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.is(ModBlocks.VAF_MULTIBLOCK_DUMMY_BLOCK.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof IMultiblockPart part) {
                BlockState mimic = part.getMimicing();
                if (mimic != null) return mimic;
            }
        }
        return state;
    }

    private static boolean structureMatches(Level level, BlockState controllerState, BlockPos controllerPos, ShapePart[] shape) {
        if (level == null || level.isClientSide) return false;

        Direction facing = controllerState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for (ShapePart part : shape) {
            BlockPos testPos = controllerPos.offset(rotateY(part.offset(), facing));
            BlockState check = effectiveState(level, testPos);

            if (!part.rule().test(check)) return false;
        }
        return true;
    }

    public static void tryForm(Level level, BlockState state, BlockPos pos, ShapePart[] shape, @Nullable BooleanProperty formedProp) {
        if (level == null || level.isClientSide) return;

        structureMatchesDebug(level, state, pos, shape);

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IMultiblockController controller)) return;

        boolean valid = structureMatches(level, state, pos, shape);

        if (valid && !controller.isFormed()) {
            linkParts(level, state, pos, shape, formedProp);
            controller.setFormed(true);
            setControllerFormedProp(level, pos, true, formedProp);
        } else if (!valid && controller.isFormed()) {
            unform(level, state, pos, shape, formedProp);
        }

    }

    public static void unform(Level level, BlockState state, BlockPos pos, ShapePart[] shape, @Nullable BooleanProperty formedProp) {
        if (level == null || level.isClientSide) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IMultiblockController controller)) return;

        controller.setFormed(false);
        setControllerFormedProp(level, pos, false, formedProp);

        unlinkParts(level, level.getBlockState(pos), pos, shape, formedProp);

    }

    public static void unformForRemoval(Level level, BlockState state, BlockPos pos, ShapePart[] shape, @Nullable BooleanProperty formedProp) {
        if (level == null || level.isClientSide) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IMultiblockController controller)) return;

        controller.setFormed(false);
        unlinkParts(level, state, pos, shape, formedProp);

    }

    private static void unlinkParts(Level level, BlockState controllerState, BlockPos controllerPos, ShapePart[] shape, @Nullable BooleanProperty formedProp) {
        if (level == null || level.isClientSide) return;

        Direction facing = controllerState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for (ShapePart part : shape) {
            BlockPos partPos = controllerPos.offset(rotateY(part.offset(), facing));

            BlockEntity be = level.getBlockEntity(partPos);
            if (!(be instanceof IMultiblockPart dummy)) {
                // Don't return — just skip this position
                continue;
            }

            // Only undo if this dummy belongs to this controller
            if (!controllerPos.equals(dummy.getControllerPos())) continue;

            BlockState restore = dummy.getMimicing();
            if (restore == null) restore = Blocks.AIR.defaultBlockState();

            // Restore the original block
            level.setBlock(partPos, restore, Block.UPDATE_ALL);

            // Clear ownership (not strictly necessary since BE is being removed by setBlock)
            dummy.setControllerPos(null);
            be.setChanged();
        }
    }

    private static void linkParts(Level level, BlockState controllerState, BlockPos controllerPos, ShapePart[] shape, @Nullable BooleanProperty formedProp) {
        if (level == null || level.isClientSide) return;

        Direction facing = controllerState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for (ShapePart part : shape) {
            BlockPos partPos = controllerPos.offset(rotateY(part.offset(), facing));

            // 1) Capture the original state BEFORE replacing
            BlockState original = level.getBlockState(partPos);

            // Optional: don't replace the controller itself
            if (partPos.equals(controllerPos)) continue;

            // 2) Replace with dummy block unconditionally
            level.setBlock(partPos, ModBlocks.VAF_MULTIBLOCK_DUMMY_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);

            // 3) Now fetch the dummy BE (might be null in edge cases)
            BlockEntity be = level.getBlockEntity(partPos);
            if (!(be instanceof IMultiblockPart dummy)) {
                // If this happens, your dummy block probably isn't creating its BE
                Aphelion.LOGGER.warn("[Multiblock] Dummy BE missing at {}", partPos);
                continue;
            }

            // 4) Set ownership + mimic state
            dummy.setControllerPos(controllerPos);
            dummy.setMimicing(original);
            be.setChanged();
        }
    }

    public static boolean structureMatchesDebug(
            Level level,
            BlockState controllerState,
            BlockPos controllerPos,
            ShapePart[] shape
    ) {
        if (level == null || level.isClientSide) return false;

        Direction facing = controllerState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for (ShapePart part : shape) {
            BlockPos rotated = rotateY(part.offset(), facing);
            BlockPos worldPos = controllerPos.offset(rotated);
            BlockState check = effectiveState(level, worldPos);

            BlockState found = level.getBlockState(worldPos);

            if (!part.rule().test(check)) {
                Aphelion.LOGGER.debug("[Multiblock] FAIL at offset=" + part.offset()
                        + " facing=" + facing
                        + " rotated=" + rotated
                        + " worldPos=" + worldPos
                        + " found=" + check.getBlock());
                return false;
            }
        }

        Aphelion.LOGGER.debug("[Multiblock] OK facing={} controllerPos={}", controllerState.getValue(BlockStateProperties.HORIZONTAL_FACING), controllerPos);
        return true;
    }

    private static void setControllerFormedProp(Level level, BlockPos pos, boolean formed, @Nullable BooleanProperty formedProp) {
        if (formedProp == null) return;

        BlockState cur = level.getBlockState(pos);
        if (!cur.hasProperty(formedProp)) { return; }
        if (cur.getValue(formedProp) == formed) { return; }

        BlockState before = level.getBlockState(pos);
        level.setBlock(pos, cur.setValue(formedProp, formed), Block.UPDATE_ALL);
        level.sendBlockUpdated(pos, before, level.getBlockState(pos), 3);

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IMultiblockController controller) {
            controller.setFormed(formed);
            be.setChanged();
        }

    }

}
