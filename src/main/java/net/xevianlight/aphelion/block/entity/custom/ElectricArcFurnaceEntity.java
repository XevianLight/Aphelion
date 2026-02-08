package net.xevianlight.aphelion.block.entity.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.xevianlight.aphelion.block.custom.ElectricArcFurnace;
import net.xevianlight.aphelion.block.custom.base.TickableBlockEntity;
import net.xevianlight.aphelion.block.entity.energy.ModEnergyStorage;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import net.xevianlight.aphelion.core.init.ModBlocks;
import net.xevianlight.aphelion.recipe.ElectricArcFurnaceRecipe;
import net.xevianlight.aphelion.recipe.ElectricArcFurnaceRecipeInput;
import net.xevianlight.aphelion.recipe.ModRecipes;
import net.xevianlight.aphelion.screen.ElectricArcFurnaceMenu;
import net.xevianlight.aphelion.util.AphelionBlockStateProperties;
import net.xevianlight.aphelion.util.MultiblockHelper;
import net.xevianlight.aphelion.util.SidedSlotHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ElectricArcFurnaceEntity extends BlockEntity implements MenuProvider, TickableBlockEntity, IArcFurnaceLike {

    private final int SIZE = 4;
    private int ENERGY_CAPACITY = 64000;
    private int MAX_TRANSFER = 320;
    private int progress = 0;
    private int maxProgress = 100;
    private final int DEFAULT_MAX_PROGRESS = 100;
    private final ContainerData data;
    private int MACHINE_ENERGY_COST = 20;

    public static final int INPUT_SLOT = 0;
    public static final int SECONDARY_INPUT_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int ENERGY_SLOT = 3;

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

    @Override
    public void serverTick(ServerLevel level, long time, BlockState state, BlockPos pos) {

        chargeFromItem();

        if (inventory.getStackInSlot(SECONDARY_INPUT_SLOT).isEmpty()) {
            // Secondary slot is empty, try furnace recipes
            if (hasFurnaceRecipe(INPUT_SLOT) && hasEnoughEnergyToCraft(MACHINE_ENERGY_COST)) {
                // Recipe detected! We have enough energy to process
                progress++;
                useEnergy();
                level.setBlockAndUpdate(pos, state.setValue(ElectricArcFurnace.LIT, true));
                setChanged(level, pos, state);

                if (hasCraftingFinished()) {
                    outputBlastingResult(INPUT_SLOT, OUTPUT_SLOT);
                    resetProgress();
                }
            } else if (hasFurnaceRecipe(INPUT_SLOT) && !hasEnoughEnergyToCraft(MACHINE_ENERGY_COST)) {
                // Recipe detected but we ran out of power
                level.setBlockAndUpdate(pos, state.setValue(ElectricArcFurnace.LIT, false));
                setChanged(level, pos, state);
                progress = progress > 0 ? progress - 1 : 0;
            } else {
                // Invalid recipe
                resetProgress();
                level.setBlockAndUpdate(pos, state.setValue(ElectricArcFurnace.LIT, false));
                setChanged(level, pos, state);
            }
        } else {
            // Secondary slot is NOT empty, try alloying recipes
            if (hasAlloyingRecipe(INPUT_SLOT, SECONDARY_INPUT_SLOT)) {
                if (hasEnoughEnergyToCraft(MACHINE_ENERGY_COST)) {
                    // Alloy recipe detected! We have enough energy to process
                    progress++;
                    useEnergy();
                    level.setBlockAndUpdate(pos, state.setValue(ElectricArcFurnace.LIT, true));
                    setChanged(level, pos, state);

                    if (hasCraftingFinished()) {
                        outputAlloyingResult(INPUT_SLOT, SECONDARY_INPUT_SLOT, OUTPUT_SLOT);
                        resetProgress();
                    }
                } else {
                    // Recipe detected but we ran out of power
                    level.setBlockAndUpdate(pos, state.setValue(ElectricArcFurnace.LIT, false));
                    setChanged(level, pos, state);
                    progress = progress > 0 ? progress - 1 : 0;
                }
            } else {
                // Invalid recipe
                resetProgress();
                level.setBlockAndUpdate(pos, state.setValue(ElectricArcFurnace.LIT, false));
                setChanged(level, pos, state);
            }
        }
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

    private void outputAlloyingResult(int inputSlot, int secondaryInputSlot, int outputSlot) {
        Optional<RecipeHolder<ElectricArcFurnaceRecipe>> recipe = getAlloyingRecipe(inventory.getStackInSlot(inputSlot), inventory.getStackInSlot(secondaryInputSlot));
        ItemStack output = recipe.get().value().getResultItem(null);

        inventory.extractItem(inputSlot, recipe.get().value().baseCount(), false);
        inventory.extractItem(secondaryInputSlot, recipe.get().value().alloyCount(), false);
        inventory.setStackInSlot(outputSlot, new ItemStack(output.getItem(), inventory.getStackInSlot(outputSlot).getCount() + (output.getCount())));
    }

    private void resetProgress() {
        this.progress = 0;
        this.maxProgress = DEFAULT_MAX_PROGRESS;
    }

    private void useEnergy() {
        this.ENERGY_STORAGE.extractEnergy(MACHINE_ENERGY_COST, false);
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
        maxProgress = DEFAULT_MAX_PROGRESS;
        return this.progress >= this.maxProgress;
    }


    private boolean hasAlloyingRecipe(int slotBase, int slotAlloy) {
        ItemStack baseStack = inventory.getStackInSlot(slotBase);
        ItemStack alloyStack = inventory.getStackInSlot(slotAlloy);

        Optional<RecipeHolder<ElectricArcFurnaceRecipe>> opt =
                getAlloyingRecipe(baseStack, alloyStack);

        if (opt.isEmpty()) return false;

        ElectricArcFurnaceRecipe recipe = opt.get().value();

        // 1) Check required input counts
        if (baseStack.getCount() < recipe.baseCount()) return false;
        if (alloyStack.getCount() < recipe.alloyCount()) return false;

        // 2) Check output slot compatibility + space
        ItemStack output = recipe.getResultItem(null);

        maxProgress = recipe.cookTime();

        return canInsertItemIntoOutputSlot(output, OUTPUT_SLOT)
                && canInsertAmountIntoOutputSlot(output.getCount(), OUTPUT_SLOT);
    }

    private Optional<RecipeHolder<ElectricArcFurnaceRecipe>> getAlloyingRecipe(ItemStack base, ItemStack alloy) {
        if (base.isEmpty()) return Optional.empty();
        if (alloy.isEmpty()) return Optional.empty();

        return this.level.getRecipeManager()
                .getRecipeFor(
                        ModRecipes.ELECTRIC_ARC_FURNACE_RECIPE_TYPE.get(),
                        new ElectricArcFurnaceRecipeInput(base, alloy),
                        level
                );
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

    @Override
    public IEnergyStorage getTrueEnergyStorage() {
        return ENERGY_STORAGE;
    }

    private final IItemHandler fullHandler = new SidedSlotHandler(inventory, new int[]{INPUT_SLOT, SECONDARY_INPUT_SLOT, OUTPUT_SLOT, ENERGY_SLOT}, true, true);
    private final IItemHandler emptyJeiHandler = new SidedSlotHandler(inventory, new int[]{}, false, false);
    private final IItemHandler inputHandler = new SidedSlotHandler(inventory, new int[]{0,1}, true, true);
    private final IItemHandler outputHandler = new SidedSlotHandler(inventory, new int[]{2,3}, false, true);
    private final IItemHandler jadeHandler = new SidedSlotHandler(inventory, new int[]{0}, false, false);

    public IItemHandler getItemHandler(Direction direction) {
        return fullHandler;
    }

    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        return ENERGY_STORAGE;
    }

    public IEnergyStorage getTrueEnergyStorage(@Nullable Direction direction) {
        return this.ENERGY_STORAGE;
    }

    private final ModEnergyStorage ENERGY_STORAGE = createEnergyStorage(ENERGY_CAPACITY, MAX_TRANSFER);
    private final ModEnergyStorage NULL_ENERGY_STORAGE = createEnergyStorage(0, 0);

    private ModEnergyStorage createEnergyStorage(int capacity, int transfer) {
        return new ModEnergyStorage(capacity, transfer) {
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

    @Override
    public ModEnergyStorage getEnergy() {
        return ENERGY_STORAGE;
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

    private static BlockPos rotateY(BlockPos off, Direction facing) {
        // Assumes "default" shape faces NORTH.
        return switch (facing) {
            case NORTH -> off;
            case EAST  -> new BlockPos(-off.getZ(), off.getY(), off.getX());
            case SOUTH -> new BlockPos(-off.getX(), off.getY(), -off.getZ());
            case WEST  -> new BlockPos(off.getZ(), off.getY(), -off.getX());
            default    -> off;
        };
    }

    public static final MultiblockHelper.ShapePart[] SHAPE = new MultiblockHelper.ShapePart[] {
        new MultiblockHelper.ShapePart(new BlockPos(3,0,3), s -> s.is(ModBlocks.ARC_FURNACE_CASING_BLOCK)),
//        new MultiblockHelper.ShapePart(new BlockPos(-1,0,0), s -> s.is(Blocks.AMETHYST_BLOCK))
    };

    @Override
    public void clientTick(ClientLevel level, long time, BlockState state, BlockPos pos) {

    }

    @Override
    public void firstTick(Level level, BlockState state, BlockPos pos) {

    }
}
