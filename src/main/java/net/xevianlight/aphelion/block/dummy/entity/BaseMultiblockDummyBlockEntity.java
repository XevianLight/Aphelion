package net.xevianlight.aphelion.block.dummy.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.entity.energy.ModEnergyStorage;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import net.xevianlight.aphelion.util.IMultiblockController;
import net.xevianlight.aphelion.util.IMultiblockPart;
import net.xevianlight.aphelion.util.SidedSlotHandler;
import org.jetbrains.annotations.Nullable;

public class BaseMultiblockDummyBlockEntity extends BlockEntity implements IMultiblockPart {
    @Nullable private BlockPos controllerPos;
    private BlockState mimicking = Blocks.AIR.defaultBlockState();

    @Nullable
    private ItemStackHandler getControllerInventory() {
        if (level == null) return null;

        BlockPos cPos = getControllerPos();
        if (cPos == null) return null;

        BlockEntity be = level.getBlockEntity(cPos);
        if (be instanceof IMultiblockController controller) {
            return controller.getInventory();
        }
        return null;
    }

    public IItemHandler getItemHandler(@Nullable Direction direction) {
        ItemStackHandler inv = getControllerInventory();
        if (inv == null) return new ItemStackHandler(0); // or null, depending on your needs

        // IMPORTANT: your indices are almost certainly 0..3, not 1..4
        return new SidedSlotHandler(inv, new int[]{0,1,2,3}, true, true);
    }

    @Nullable
    private ModEnergyStorage getControllerEnergy() {
        if (level == null) return null;

        BlockPos cPos = getControllerPos();
        if (cPos == null) return null;

        BlockEntity be = level.getBlockEntity(cPos);
        if (be instanceof IMultiblockController controller) {
            return controller.getEnergy();
        }
        return null;
    }

    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        ModEnergyStorage nrg = getControllerEnergy();
        if (nrg == null) return new ModEnergyStorage(0, 0) {
            @Override
            public void onEnergyChanged() {

            }
        };

        return nrg;
    }

//    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
//        if (direction == null)
//            return isFormed() ? ENERGY_STORAGE : NULL_ENERGY_STORAGE;
//        return isFormed() ? ENERGY_STORAGE : null;
//    }

    public BaseMultiblockDummyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public void setControllerPos(@Nullable BlockPos pos) {
        controllerPos = pos;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            invalidateCapabilities();
        }
    }

    public @Nullable BlockPos getControllerPos() {
        return controllerPos;
    }

    @Override
    public void onDummyBroken() {
        if (level == null || level.isClientSide) return;

        if (controllerPos == null) {
            level.setBlock(getBlockPos(), getMimicing(), Block.UPDATE_ALL);
            return;
        }

        BlockEntity be = level.getBlockEntity(controllerPos);
        if (be instanceof IMultiblockController controller) {
            controller.markDirty();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (controllerPos != null) tag.putLong("controller", controllerPos.asLong());
        tag.put("mimic", NbtUtils.writeBlockState(mimicking));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        controllerPos = tag.contains("controller") ? BlockPos.of(tag.getLong("controller")) : null;
        if (tag.contains("mimic")) {
            // Depending on your version, this line may need the registry lookup variant.
            setMimicing(NbtUtils.readBlockState(registries.lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK), tag.getCompound("mimic")));
        }
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net,
                             ClientboundBlockEntityDataPacket pkt,
                             HolderLookup.Provider registries) {

        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            loadAdditional(tag, registries);
        }

        // Force rerender on client
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            requestModelDataUpdate(); // if you rely on model partitionData
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);

        // CLIENT: force rerender
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public BlockState getMimicing() {
        return mimicking;
    }

    @Override
    public void setMimicing(BlockState newState) {
        mimicking = newState;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            requestModelDataUpdate(); // only if you use model partitionData
        }
    }
}
