package net.xevianlight.aphelion.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.xevianlight.aphelion.Aphelion;
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

    private static boolean structureMatches(Level level, BlockState state, BlockPos pos, ShapePart[] shape) {
        if (level == null || level.isClientSide) return false;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for (ShapePart part : shape) {
            BlockPos testPos = pos.offset(rotateY(part.offset(), facing));
            BlockState check = level.getBlockState(testPos);

            if (!part.rule().test(check)) {
                return false;
            }
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
            controller.setFormed(true);
            linkParts(level, state, pos, shape, formedProp);
            if (state.hasProperty(AphelionBlockStateProperties.FORMED) && !state.getValue(AphelionBlockStateProperties.FORMED)) {
                level.setBlock(pos, state.setValue(AphelionBlockStateProperties.FORMED, true), 3);
            }
        } else if (!valid && controller.isFormed()) {
            unform(level, state, pos, shape, formedProp);
        }
    }

    public static void unform(Level level, BlockState state, BlockPos pos, ShapePart[] shape, @Nullable BooleanProperty formedProp) {
        if (level == null || level.isClientSide) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IMultiblockController controller)) return;

        controller.setFormed(false);
        if (state.hasProperty(AphelionBlockStateProperties.FORMED) && state.getValue(AphelionBlockStateProperties.FORMED)) {
            level.setBlock(pos, state.setValue(AphelionBlockStateProperties.FORMED, false), 3);
        }

        unlinkParts(level, state, pos, shape, formedProp);

    }

    public static void unformForRemoval(Level level, BlockState state, BlockPos pos, ShapePart[] shape, @Nullable BooleanProperty formedProp) {
        if (level == null || level.isClientSide) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IMultiblockController controller)) return;

        controller.setFormed(false);
        unlinkParts(level, state, pos, shape, formedProp);

    }

    private static void unlinkParts(Level level, BlockState state, BlockPos pos, ShapePart[] shape, @Nullable BooleanProperty formedProp) {
        if (level == null || level.isClientSide) return;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for (ShapePart part : shape) {
            BlockPos partPos = pos.offset(rotateY(part.offset(), facing));
            BlockState st = level.getBlockState(partPos);

            if (formedProp != null && st.hasProperty(formedProp) && st.getValue(formedProp)) {
                level.setBlock(partPos, st.setValue(formedProp, false), 3);
            }

            BlockEntity be = level.getBlockEntity(partPos);
            if (be instanceof IMultiblockPart mbPart) {
                if (pos.equals(mbPart.getControllerPos())) {
                    mbPart.setControllerPos(null);
                    be.setChanged();
                }
            }
        }

    }

    private static void linkParts(Level level, BlockState state, BlockPos pos, ShapePart[] shape, @Nullable BooleanProperty formedProp) {
        if (level == null || level.isClientSide) return;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for (ShapePart part : shape) {
            BlockPos partPos = pos.offset(rotateY(part.offset(), facing));
            BlockState st = level.getBlockState(partPos);

            Aphelion.LOGGER.debug("[Multiblock] partPos={} block={} hasFormed={} formedVal={}",
                    partPos,
                    st.getBlock(),
                    (formedProp != null && st.hasProperty(formedProp)),
                    (formedProp != null && st.hasProperty(formedProp)) ? st.getValue(formedProp) : "n/a"
            );

            if (formedProp != null && st.hasProperty(formedProp) && !st.getValue(formedProp)) {
                level.setBlock(partPos, st.setValue(formedProp, true), 3);
                st = level.getBlockState(partPos);
            }

            BlockEntity be = level.getBlockEntity(partPos);
            if (be instanceof IMultiblockPart mbPart) {
                if (!pos.equals(mbPart.getControllerPos())) {
                    mbPart.setControllerPos(pos);
                    be.setChanged();
                }
            }
        }
    }

    public static boolean structureMatchesDebug(
            Level level,
            BlockState controllerState,
            BlockPos controllerPos,
            ShapePart[] shape
    ) {
        Direction facing = controllerState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for (ShapePart part : shape) {
            BlockPos rotated = rotateY(part.offset(), facing);
            BlockPos worldPos = controllerPos.offset(rotated);

            BlockState found = level.getBlockState(worldPos);

            if (!part.rule().test(found)) {
                Aphelion.LOGGER.debug("[Multiblock] FAIL at offset=" + part.offset()
                        + " facing=" + facing
                        + " rotated=" + rotated
                        + " worldPos=" + worldPos
                        + " found=" + found.getBlock());
                return false;
            }
        }

        Aphelion.LOGGER.debug("[Multiblock] OK facing={} controllerPos={}", controllerState.getValue(BlockStateProperties.HORIZONTAL_FACING), controllerPos);
        return true;
    }

}
