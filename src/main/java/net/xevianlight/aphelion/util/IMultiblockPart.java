package net.xevianlight.aphelion.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface IMultiblockPart {
    @Nullable BlockPos getControllerPos();

    void setControllerPos(@Nullable BlockPos pos);

    BlockState getMimicing();

    void setMimicing(BlockState newState);

    void onDummyBroken();
}