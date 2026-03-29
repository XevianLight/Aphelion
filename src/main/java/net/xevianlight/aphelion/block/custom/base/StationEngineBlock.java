package net.xevianlight.aphelion.block.custom.base;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StationEngineBlock extends BasicEntityBlock {

    public static final MapCodec<StationEngineBlock> CODEC = simpleCodec(StationEngineBlock::new);

    protected StationEngineBlock(Properties properties) {
        super(properties, true);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return null;
    }
}
