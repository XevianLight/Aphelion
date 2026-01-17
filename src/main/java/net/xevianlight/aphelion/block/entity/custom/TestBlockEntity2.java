package net.xevianlight.aphelion.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class TestBlockEntity2 extends BaseContainerBlockEntity implements WorldlyContainer {

    // The constructor, like before.
    public TestBlockEntity2 (BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.TEST_BLOCK_ENTITY.get(), pos, blockState);
    }
    // The container size. This can of course be any value you want.
    public static final int SIZE = 9;
    // Our item stack list. This is not final due to #setItems existing.
    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    // The container size, like before.
    @Override
    public int getContainerSize() {
        return SIZE;
    }

    // The getter for our item stack list.
    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    // The setter for our item stack list.
    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    // The display name of the menu. Don't forget to add a translation!
    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.examplemod.myblockentity");
    }

    // The menu to create from this container. See below for what to return here.
    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return null;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[] {0,1,2,3,4,5,6,7,8};
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return true;
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, this.items, provider);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        ContainerHelper.saveAllItems(nbt, this.items, provider);
    }

    public void dropContents() {
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = getItem(i);
            Containers.dropItemStack(level, (double) worldPosition.getX(), (double) worldPosition.getY(), (double) worldPosition.getZ(), stack);
        }
    }
}
