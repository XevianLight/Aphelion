package net.xevianlight.aphelion.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import net.xevianlight.aphelion.util.IMultiblockPart;
import org.jetbrains.annotations.Nullable;

public class EAFPartEntity extends BlockEntity implements IMultiblockPart {
    @Nullable private BlockPos controllerPos;

    public EAFPartEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.EAF_PART_ENTITY.get(), pos, blockState);
    }

    public void setControllerPos(@Nullable BlockPos pos) {
        controllerPos = pos;
        setChanged();
    }

    public @Nullable BlockPos getControllerPos() {
        return controllerPos;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (controllerPos != null) tag.putLong("controller", controllerPos.asLong());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        controllerPos = tag.contains("controller") ? BlockPos.of(tag.getLong("controller")) : null;
    }
}
