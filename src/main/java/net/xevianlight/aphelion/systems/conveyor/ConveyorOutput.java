package net.xevianlight.aphelion.systems.conveyor;

import net.minecraft.world.item.ItemStack;

public interface ConveyorOutput {
    /// @return Rejected items
    ItemStack insertItem(ItemStack stack, boolean simulate);
}
