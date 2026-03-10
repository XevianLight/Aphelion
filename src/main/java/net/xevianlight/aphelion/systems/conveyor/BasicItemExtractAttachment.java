package net.xevianlight.aphelion.systems.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.xevianlight.aphelion.block.entity.custom.PipeTestBlockEntity;
import org.jetbrains.annotations.Nullable;

public class BasicItemExtractAttachment extends ConveyorAttachment implements ConveyorInput {

    BlockCapabilityCache<IItemHandler, @Nullable Direction> capabilityCache;

    public BasicItemExtractAttachment(ServerLevel level, BlockPos pos, Direction facingDirection) {
        super(level, pos, facingDirection);

        capabilityCache = BlockCapabilityCache.create(
                Capabilities.ItemHandler.BLOCK,
                level,
                pos.relative(facingDirection),
                facingDirection.getOpposite()
        );
    }

    @Override
    public void render() {

    }

    @Override
    public void tick(ServerLevel level, BlockState state, BlockPos pos, Direction facingDirection, ConveyorNetwork network) {
        // do an extract and distribute
        IItemHandler container = capabilityCache.getCapability();
        if (container == null) return;

        final int EXTRACT_PER_TICK = 4;
        int to_extract = EXTRACT_PER_TICK;

        int extract_slot_id = container.getSlots() - 1;
        while (extract_slot_id >= 0 && container.getStackInSlot(extract_slot_id).isEmpty()) extract_slot_id--;
        if (extract_slot_id == -1) return;


        // Do a simulated extract, then run through every output side on the graph and do real inserts.
        // By the end, remove as many items as we successfully inserted with a real extract
        ItemStack to_distribute = container.extractItem(extract_slot_id, to_extract, true);
        int extracted_amount = to_distribute.getCount();

        to_distribute = network.insertItem(to_distribute, false);

        int distributed_amount = extracted_amount;
        if (!to_distribute.isEmpty()) {
            distributed_amount = extracted_amount - to_distribute.getCount();
        }
        container.extractItem(extract_slot_id, distributed_amount, false);
    }
}