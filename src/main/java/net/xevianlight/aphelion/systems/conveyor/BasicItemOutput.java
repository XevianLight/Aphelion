package net.xevianlight.aphelion.systems.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class BasicItemOutput implements ConveyorOutput {
    BlockCapabilityCache<IItemHandler, @Nullable Direction> capabilityCache;

    public BasicItemOutput(ServerLevel level, BlockPos pos, Direction facingDirection) {
        capabilityCache = BlockCapabilityCache.create(
                Capabilities.ItemHandler.BLOCK,
                level,
                pos.relative(facingDirection),
                facingDirection.getOpposite()
        );
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        ItemStack toInsert = stack.copy();

        IItemHandler container = capabilityCache.getCapability();
        if (container == null) return toInsert;

        for (int insertIndex = 0; insertIndex < container.getSlots(); insertIndex++) {
            if (toInsert.isEmpty()) break;

            toInsert = container.insertItem(insertIndex, toInsert, simulate);
        }

        return toInsert;
    }
}
