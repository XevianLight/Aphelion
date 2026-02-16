package net.xevianlight.aphelion.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// a lot of this is ai slop so take it with a grainlet of salt
public class PipeTestBlock extends Block {

    // shortcuts for each directional property because they're pretty verbose
    // t/f here means is/isn't connected outgoing in that direction
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    // Voxel shape pieces to make the actual pipe model
    private static final VoxelShape CORE = Block.box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape NORTH_SHAPE = Block.box(6, 6, 0, 10, 10, 6);
    private static final VoxelShape SOUTH_SHAPE = Block.box(6, 6, 10, 10, 10, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(10, 6, 6, 16, 10, 10);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 6, 6, 6, 10, 10);
    private static final VoxelShape UP_SHAPE = Block.box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape DOWN_SHAPE = Block.box(6, 0, 6, 10, 6, 10);

    // Assembles the "VoxelShape" of the block which i assume is just the collision/place/mine hitbox
    // later this should be cached for each different shape that we need
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;

        if (state.getValue(NORTH)) shape = Shapes.join(shape, NORTH_SHAPE, BooleanOp.OR);
        if (state.getValue(SOUTH)) shape = Shapes.join(shape, SOUTH_SHAPE, BooleanOp.OR);
        if (state.getValue(EAST))  shape = Shapes.join(shape, EAST_SHAPE, BooleanOp.OR);
        if (state.getValue(WEST))  shape = Shapes.join(shape, WEST_SHAPE, BooleanOp.OR);
        if (state.getValue(UP))    shape = Shapes.join(shape, UP_SHAPE, BooleanOp.OR);
        if (state.getValue(DOWN))  shape = Shapes.join(shape, DOWN_SHAPE, BooleanOp.OR);

        return shape;
    }

    public PipeTestBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false));
    }

    // This method determines the state when first placed
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return makeConnections(context.getLevel(), context.getClickedPos());
    }

    // Updates the block when a neighbor changes
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        return makeConnections(level, currentPos);
    }

    public static Properties getProperties() {
        return Properties.of().noOcclusion();
    }

    private BlockState makeConnections(LevelAccessor level, BlockPos pos) {
        return this.defaultBlockState()
                .setValue(NORTH, canConnect(level, pos.north()))
                .setValue(SOUTH, canConnect(level, pos.south()))
                .setValue(EAST, canConnect(level, pos.east()))
                .setValue(WEST, canConnect(level, pos.west()))
                .setValue(UP, canConnect(level, pos.above()))
                .setValue(DOWN, canConnect(level, pos.below()));
    }

    private boolean canConnect(LevelAccessor level, BlockPos neighborPos) {
        // Simplest logic: connect if the neighbor is also a SimplePipeBlock
        return level.getBlockState(neighborPos).getBlock() == this;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    // Doesn't block sunlight
//    @Override
//    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
//        return true;
//    }

    /// This function doesn't do what the AI says it does. I have no idea what it actually does, though
    // 2. Prevents the "black shadows" inside or around the pipe
//    @Override
//    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
//        return 0;
//    }

    /// I don't know what this does, because Properties.noOcclusion() is the part that made the leaves keep
    /// rendering their faces.
    // 3. Tells adjacent blocks (like leaves) to keep rendering their faces
//    @Override
//    public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
//        return false;
//    }

    // This bit affects how strong the ambient occlusion effect is
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F; // Maintains full brightness
    }
}
