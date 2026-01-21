package net.xevianlight.aphelion.block.dummy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xevianlight.aphelion.block.dummy.entity.VAFMultiblockDummyBlockEntity;
import org.jetbrains.annotations.Nullable;

public class VAFMultiblockDummyBlock extends BaseMultiblockDummyBlock {
    public VAFMultiblockDummyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new VAFMultiblockDummyBlockEntity(blockPos, blockState);
    }
}
