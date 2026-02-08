package net.xevianlight.aphelion.block.entity.custom;

import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.xevianlight.aphelion.block.entity.energy.ModEnergyStorage;


public interface IArcFurnaceLike {
    ItemStackHandler getInventory();
    ModEnergyStorage getEnergy();

    void sendUpdate();

    BlockState getBlockState();

    IEnergyStorage getTrueEnergyStorage();
}
