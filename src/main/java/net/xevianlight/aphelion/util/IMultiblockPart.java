package net.xevianlight.aphelion.util;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface IMultiblockPart {
    @Nullable BlockPos getControllerPos();

    void setControllerPos(@Nullable BlockPos pos);
}
