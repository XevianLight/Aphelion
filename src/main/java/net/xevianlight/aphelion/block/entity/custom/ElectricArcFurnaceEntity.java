package net.xevianlight.aphelion.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.custom.ElectricArcFurnace;
import net.xevianlight.aphelion.block.entity.energy.ModEnergyStorage;
import net.xevianlight.aphelion.block.entity.energy.ModEnergyUtil;
import net.xevianlight.aphelion.client.dimension.DimensionRendererCache;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import net.xevianlight.aphelion.screen.ElectricArcFurnaceMenu;
import net.xevianlight.aphelion.util.SidedSlotHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ElectricArcFurnaceEntity extends BlockEntity implements MenuProvider {

    private final int SIZE = 4;
    private int ENERGY_CAPACITY = 64000;
    private int MAX_TRANSFER = 320;
    private int progress = 0;
    private int maxProgress = 100;
    private final int DEFAULT_MAX_PROGRESS = 100;
    private final ContainerData data;
    private int Blasting_ENERGY_COST = 20;

    private final int INPUT_SLOT = 0;
    private final int SECONDARY_INPUT_SLOT = 1;
    private final int OUTPUT_SLOT = 2;
    private final int ENERGY_SLOT = 3;

    public ElectricArcFurnaceEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ELECTRIC_ARC_FURNACE_ENTITY.get(), pos, blockState);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> ElectricArcFurnaceEntity.this.progress;
                    case 1 -> ElectricArcFurnaceEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int pValue) {
                switch (index) {
                    case 0: ElectricArcFurnaceEntity.this.progress = pValue;
                    case 1: ElectricArcFurnaceEntity.this.maxProgress = pValue;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    public final ItemStackHandler inventory = new ItemStackHandler(SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

//        @Override
//        public boolean isItemValid(int slot, ItemStack stack) {
//            if (slot == ENERGY_SLOT) {
//                var capability = stack.getCapability(Capabilities.EnergyStorage.ITEM);
//                return capability != null;
//            }
//            return super.isItemValid(slot, stack);
//        }
    };

    public void tick(Level level, BlockPos pos, BlockState blockState) {

        if (inventory.getStackInSlot(SECONDARY_INPUT_SLOT).isEmpty()) {
            if (hasFurnaceRecipe(INPUT_SLOT) && hasEnoughEnergyToCraft(Blasting_ENERGY_COST)) {
                progress++;
                useEnergyForBlasting();
                level.setBlockAndUpdate(pos, blockState.setValue(ElectricArcFurnace.LIT, true));
                setChanged(level, pos, blockState);

                if (hasCraftingFinished()) {
                    outputBlastingResult(INPUT_SLOT, OUTPUT_SLOT);
                    resetProgress();
                }
            } else if (hasFurnaceRecipe(INPUT_SLOT) && !hasEnoughEnergyToCraft(Blasting_ENERGY_COST)) {
                level.setBlockAndUpdate(pos, blockState.setValue(ElectricArcFurnace.LIT, false));
                setChanged(level, pos, blockState);
                progress = progress > 0 ? progress - 1 : 0;
            } else {
                resetProgress();
                level.setBlockAndUpdate(pos, blockState.setValue(ElectricArcFurnace.LIT, false));
                setChanged(level, pos, blockState);
            }
        } else {
            resetProgress();
            level.setBlockAndUpdate(pos, blockState.setValue(ElectricArcFurnace.LIT, false));
            setChanged(level, pos, blockState);
        }

        chargeFromItem();
    }

    private void chargeFromItem() {
        ItemStack stack;

        try {
            stack = inventory.getStackInSlot(ENERGY_SLOT);

            if (stack.isEmpty()) return;

            IEnergyStorage itemEnergy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (itemEnergy == null || !itemEnergy.canExtract()) return;

            int freeCapacity = ENERGY_STORAGE.getMaxEnergyStored() - ENERGY_STORAGE.getEnergyStored();
            if (freeCapacity <= 0) return;

            int maxMove = Math.min(MAX_TRANSFER, freeCapacity);

            // Simulate extraction first
            int canExtract = itemEnergy.extractEnergy(maxMove, true);
            if (canExtract <= 0) return;

            // Receive into block (simulate then execute is safest)
            int canReceive = ENERGY_STORAGE.receiveEnergy(canExtract, true);
            if (canReceive <= 0) return;

            int extracted = itemEnergy.extractEnergy(canReceive, false);
            ENERGY_STORAGE.receiveEnergy(maxMove, false);

            setChanged();
        } catch (Exception e) {

        }
    }

    private void outputBlastingResult(int slot, int resultSlot) {
        Optional<RecipeHolder<BlastingRecipe>> recipe = getFurnaceRecipe(inventory.getStackInSlot(slot));
        ItemStack output = recipe.get().value().getResultItem(null);

        // 2x multiplier for smelting recipes

        inventory.extractItem(slot, 1, false);
        inventory.setStackInSlot(resultSlot, new ItemStack(output.getItem(),
                inventory.getStackInSlot(resultSlot).getCount() + (output.getCount() * 2)));
    }

    private void resetProgress() {
        this.progress = 0;
        this.maxProgress = DEFAULT_MAX_PROGRESS;
    }

    private void useEnergyForBlasting() {
        this.ENERGY_STORAGE.extractEnergy(Blasting_ENERGY_COST, false);
    }

    private boolean hasEnoughEnergyToCraft(int energyCost) {
        return ENERGY_STORAGE.getEnergyStored() >= energyCost;
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output, int slot) {
        return inventory.getStackInSlot(slot).isEmpty() ||
                inventory.getStackInSlot(slot).getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoOutputSlot(int count, int slot) {
        int maxCount = inventory.getStackInSlot(slot).isEmpty() ? 64 : inventory.getStackInSlot(slot).getMaxStackSize();
        int currentCount = inventory.getStackInSlot(slot).getCount();

        return maxCount >= currentCount + count;
    }

    private boolean isOutputSlotEmptyOrReceivable(int slot) {
        return this.inventory.getStackInSlot(slot).isEmpty() ||
                this.inventory.getStackInSlot(slot).getCount() < this.inventory.getStackInSlot(slot).getMaxStackSize();
    }

    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }

    private boolean hasFurnaceRecipe(int slot) {
        Optional<RecipeHolder<BlastingRecipe>> recipe = getFurnaceRecipe(new ItemStack(inventory.getStackInSlot(slot).getItem().asItem(), 1));
        if (recipe.isEmpty())
            return false;

        ItemStack output = recipe.get().value().getResultItem(null);
        return canInsertAmountIntoOutputSlot(output.getCount() * 2, OUTPUT_SLOT) && canInsertItemIntoOutputSlot(output, OUTPUT_SLOT);
    }

    private Optional<RecipeHolder<BlastingRecipe>> getFurnaceRecipe(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();

        return this.level.getRecipeManager()
                .getRecipeFor(RecipeType.BLASTING, new SingleRecipeInput(stack), level);

    }

    public void sendUpdate() {
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    private final IItemHandler fullHandler = new SidedSlotHandler(inventory, new int[]{0,1}, true, true);
    private final IItemHandler inputHandler = new SidedSlotHandler(inventory, new int[]{0,1}, true, true);
    private final IItemHandler outputHandler = new SidedSlotHandler(inventory, new int[]{2,3}, false, true);
    private final IItemHandler jadeHandler = new SidedSlotHandler(inventory, new int[]{0}, false, false);

    public IItemHandler getItemHandler(Direction direction) {
        return fullHandler;
    }

    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        return this.ENERGY_STORAGE;
    }

    private final ModEnergyStorage ENERGY_STORAGE = createEnergyStorage();
    private ModEnergyStorage createEnergyStorage() {
        return new ModEnergyStorage(ENERGY_CAPACITY, MAX_TRANSFER) {
            @Override
            public void onEnergyChanged() {
                setChanged();
                getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        };
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.put("inventory", inventory.serializeNBT(pRegistries));
        tag.putInt("electric_arc_furnace.progress", progress);
        tag.putInt("electric_arc_furnace.maxProgress", maxProgress);
        tag.putInt("electric_arc_furnace.energy", ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(tag, pRegistries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(tag, pRegistries);
        inventory.deserializeNBT(pRegistries, tag.getCompound("inventory"));
        progress = tag.getInt("electric_arc_furnace.progress");
        maxProgress = tag.getInt("electric_arc_furnace.maxProgress");
        ENERGY_STORAGE.setEnergy(tag.getInt("electric_arc_furnace.energy"));
    }

    @Override
    public Component getDisplayName() {
        return null;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ElectricArcFurnaceMenu(i, inventory, this, this.data);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }
}
