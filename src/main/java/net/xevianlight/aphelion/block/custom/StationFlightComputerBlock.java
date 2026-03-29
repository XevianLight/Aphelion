package net.xevianlight.aphelion.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xevianlight.aphelion.block.custom.base.BasicHorizontalEntityBlock;
import net.xevianlight.aphelion.block.entity.custom.StationFlightComputerBlockEntity;
import net.xevianlight.aphelion.core.saveddata.types.PartitionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StationFlightComputerBlock extends BasicHorizontalEntityBlock {

    public static final MapCodec<StationFlightComputerBlock> CODEC = simpleCodec(StationFlightComputerBlock::new);

    public StationFlightComputerBlock(Properties properties) {
        super(properties, true);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new StationFlightComputerBlockEntity(blockPos, blockState);
    }

    @Override
    protected void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof StationFlightComputerBlockEntity computerBE) {
            PartitionData data = computerBE.getData();
            if (data != null) {
                data.setTraveling(false);
            }
        }
    }
}
