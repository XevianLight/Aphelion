package net.xevianlight.aphelion.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xevianlight.aphelion.block.entity.custom.OxygenTestBlockEntity;
import org.jetbrains.annotations.Nullable;

public class OxygenTestBlock extends Block implements EntityBlock {

    public OxygenTestBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new OxygenTestBlockEntity(blockPos, blockState);
    }

    public static Properties getProperties() {
        return Properties.of();
    }
}
