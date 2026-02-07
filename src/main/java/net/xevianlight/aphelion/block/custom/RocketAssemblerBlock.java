package net.xevianlight.aphelion.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.xevianlight.aphelion.block.custom.base.BasicHorizontalEntityBlock;
import net.xevianlight.aphelion.block.entity.custom.RocketAssemblerBlockEntity;
import net.xevianlight.aphelion.util.AphelionBlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RocketAssemblerBlock extends BasicHorizontalEntityBlock {

    public static final BooleanProperty FORMED = AphelionBlockStateProperties.FORMED;

    public RocketAssemblerBlock(Properties properties) {
        super(properties, true);
    }

    public static final MapCodec<RocketAssemblerBlock> CODEC = simpleCodec(RocketAssemblerBlock::new);

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public static Properties getProperties() {
        return Properties
                .of()
                .sound(SoundType.METAL)
                .destroyTime(2f)
                .explosionResistance(10f)
                .requiresCorrectToolForDrops();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new RocketAssemblerBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(FORMED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
        super.createBlockStateDefinition(builder);
    }
}
