package net.xevianlight.aphelion.block.dummy.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.xevianlight.aphelion.core.init.ModBlockEntities;

public class VAFMultiblockDummyBlockEntity extends BaseMultiblockDummyBlockEntity {
    public VAFMultiblockDummyBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.VAF_MULTIBLOCK_DUMMY_ENTITY.get(), pos, blockState);
    }
}
