package net.xevianlight.aphelion.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.xevianlight.aphelion.block.entity.custom.OxygenTestBlockEntity;
import net.xevianlight.aphelion.block.entity.custom.TestBlockEntity;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class OxygenTestBlock extends BaseEntityBlock {

    public OxygenTestBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<OxygenTestBlock> CODEC = simpleCodec(OxygenTestBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new OxygenTestBlockEntity(blockPos, blockState);
    }

    public static Properties getProperties() {
        return Properties.of();
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(blockEntityType, ModBlockEntities.OXYGEN_TEST_BLOCK_ENTITY.get(), (level1, blockPos, blockState, oxygenTestBlockEntity) -> oxygenTestBlockEntity.tick(level1, blockPos, blockState));

    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity= level.getBlockEntity(pos);
            if (blockEntity instanceof OxygenTestBlockEntity oxygenTestBlockEntity) {
                oxygenTestBlockEntity.removeEnclosed();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
