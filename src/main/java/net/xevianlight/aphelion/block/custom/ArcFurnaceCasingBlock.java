package net.xevianlight.aphelion.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.xevianlight.aphelion.block.dummy.BaseMultiblockDummyBlock;
import net.xevianlight.aphelion.block.entity.custom.EAFPartEntity;
import net.xevianlight.aphelion.block.entity.custom.VacuumArcFurnaceControllerEntity;
import net.xevianlight.aphelion.util.AphelionBlockStateProperties;
import net.xevianlight.aphelion.util.IMultiblockController;
import net.xevianlight.aphelion.util.MultiblockHelper;
import org.jetbrains.annotations.Nullable;

public class ArcFurnaceCasingBlock extends BaseEntityBlock {

    public static final BooleanProperty FORMED = AphelionBlockStateProperties.FORMED;

    public ArcFurnaceCasingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FORMED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public static Properties getProperties() {
        return Properties
                .of()
                .sound(SoundType.NETHERITE_BLOCK)
                .destroyTime(2f)
                .explosionResistance(10f)
                .requiresCorrectToolForDrops();
    }

    public static Item.Properties getItemProperties() {
        return new Item.Properties();
    }

    public static final MapCodec<ArcFurnaceCasingBlock> CODEC = simpleCodec(ArcFurnaceCasingBlock::new);

//    @Override
//    protected MapCodec<? extends BaseEntityBlock> codec() {
//        return CODEC;
//    }

//    @Override
//    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
//        return new EAFPartEntity(blockPos, blockState);
//    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) pingNearbyController(level, pos);
    }

    private void pingNearbyController(Level level, BlockPos pos) {
        int r = 5;
        BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();

        for (int dx=-r; dx<=r; dx++)
            for (int dy=-r; dy<=r; dy++)
                for (int dz=-r; dz<=r; dz++) {
                    mp.set(pos.getX()+dx, pos.getY()+dy, pos.getZ()+dz);
                    BlockEntity be = level.getBlockEntity(mp);
                    if (be instanceof IMultiblockController controller) {
                        controller.markDirty();

                    }
                }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && state.getBlock() != newState.getBlock())
            pingNearbyController(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FORMED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new EAFPartEntity(blockPos, blockState);
    }
}
