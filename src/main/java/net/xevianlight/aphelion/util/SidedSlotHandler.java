package net.xevianlight.aphelion.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class SidedSlotHandler implements IItemHandler {

    private final IItemHandlerModifiable backing;
    private final int[] slots;
    private final boolean allowInsert;
    private final boolean allowExtract;

    public SidedSlotHandler(IItemHandlerModifiable backing, int[] slots, boolean allowInsert, boolean allowExtract) {
        this.backing = backing;
        this.slots = slots;
        this.allowInsert = allowInsert;
        this.allowExtract = allowExtract;
    }

    private int toBackingSlot(int localSlot) {
        if (localSlot < 0 || localSlot >= slots.length) return -1;
        return slots[localSlot];
    }


    @Override
    public int getSlots() {
        return slots.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        int s = toBackingSlot(slot);
        return s < 0 ? ItemStack.EMPTY : backing.getStackInSlot(s);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!allowInsert || stack.isEmpty()) return stack;
        int s = toBackingSlot(slot);
        return s < 0 ? stack : backing.insertItem(s, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!allowExtract || amount <= 0) return ItemStack.EMPTY;
        int s = toBackingSlot(slot);
        return s < 0 ? ItemStack.EMPTY : backing.extractItem(s, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        int s = toBackingSlot(slot);
        return s < 0 ? 0 : backing.getSlotLimit(s);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (!allowInsert) return false;
        int s = toBackingSlot(slot);
        return s >= 0 && backing.isItemValid(s, stack);
    }
}
