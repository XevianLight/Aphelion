package net.xevianlight.aphelion.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import net.xevianlight.aphelion.screen.TestBlockMenu;
import net.xevianlight.aphelion.util.SidedSlotHandler;
import org.jetbrains.annotations.Nullable;

public class TestBlockEntity extends BlockEntity implements MenuProvider {

    private final int SIZE = 1;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public TestBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.TEST_BLOCK_ENTITY.get(), pos, blockState);
    }

    public final ItemStackHandler inventory = new ItemStackHandler(SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public void tick(Level level, BlockPos pos, BlockState blockState) {
        inventory.insertItem(0, new ItemStack(Items.DIAMOND), false);

    }

    public void sendUpdate() {
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    private final IItemHandler topHandler = new SidedSlotHandler(inventory, new int[]{0}, true, false);
    private final IItemHandler bottomHandler = new SidedSlotHandler(inventory, new int[]{0}, false, true);
    private final IItemHandler jadeHandler = new SidedSlotHandler(inventory, new int[]{0}, false, false);

    public IItemHandler getItemHandler(Direction direction) {
        if (direction == Direction.UP) return topHandler;
        if (direction == Direction.DOWN) return bottomHandler;
        if (direction == null) return jadeHandler;
        return null;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for(int i = 0; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.put("inventory", inventory.serializeNBT(pRegistries));
        super.saveAdditional(pTag, pRegistries);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        inventory.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
    }

    @Override
    public Component getDisplayName() {
        return null;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new TestBlockMenu(i, inventory, this);
    }
}
