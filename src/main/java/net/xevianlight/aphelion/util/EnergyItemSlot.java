package net.xevianlight.aphelion.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class EnergyItemSlot extends SlotItemHandler {
    public EnergyItemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.getCapability(Capabilities.EnergyStorage.ITEM) != null)
            return stack.getCapability(Capabilities.EnergyStorage.ITEM).canExtract();
        return false;
    }
}
