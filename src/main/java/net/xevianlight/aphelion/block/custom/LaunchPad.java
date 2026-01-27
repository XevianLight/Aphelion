package net.xevianlight.aphelion.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.xevianlight.aphelion.util.ModTags;
import org.jetbrains.annotations.NotNull;

public class LaunchPad extends Block {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST  = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST  = BlockStateProperties.WEST;

    public LaunchPad(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }

    private static boolean isPad(BlockState st) {
        return st.is(ModTags.Blocks.LAUNCH_PAD); // <- make a tag, recommended
    }

    private BlockState withConnections(LevelAccessor level, BlockPos pos, BlockState state) {
        return state
                .setValue(NORTH, isPad(level.getBlockState(pos.north())))
                .setValue(EAST,  isPad(level.getBlockState(pos.east())))
                .setValue(SOUTH, isPad(level.getBlockState(pos.south())))
                .setValue(WEST,  isPad(level.getBlockState(pos.west())));
    }

    public static Properties getProperties() {
        return Properties
                .of()
                .sound(SoundType.STONE)
                .destroyTime(2f)
                .explosionResistance(10f)
                .requiresCorrectToolForDrops();
    }

    public static Item.Properties getItemProperties() {
        return new Item.Properties();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return withConnections(ctx.getLevel(), ctx.getClickedPos(), this.defaultBlockState());
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state, Direction dir, @NotNull BlockState neighborState,
                                           @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (dir.getAxis().isHorizontal()) {
            return withConnections(level, pos, state);
        }
        return state;
    }

}
