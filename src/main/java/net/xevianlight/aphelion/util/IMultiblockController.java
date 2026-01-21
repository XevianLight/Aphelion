package net.xevianlight.aphelion.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.xevianlight.aphelion.block.entity.energy.ModEnergyStorage;
import org.jetbrains.annotations.Nullable;

public interface IMultiblockController {
    boolean isFormed();
    void setFormed(boolean formed);

    void markDirty();

    BlockPos getBlockPos();

    BlockState getBlockState();

    MultiblockHelper.ShapePart[] getMultiblockShape();

    String getMenuTitle();

    ItemStackHandler getInventory();

    ModEnergyStorage getEnergy();
}
