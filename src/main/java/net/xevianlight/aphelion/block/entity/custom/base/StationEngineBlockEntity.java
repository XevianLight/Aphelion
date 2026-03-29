package net.xevianlight.aphelion.block.entity.custom.base;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.xevianlight.aphelion.block.custom.base.TickableBlockEntity;
import net.xevianlight.aphelion.core.init.ModDimensions;
import net.xevianlight.aphelion.core.saveddata.SpacePartitionSavedData;
import net.xevianlight.aphelion.core.saveddata.types.PartitionData;

import javax.annotation.Nullable;

public abstract class StationEngineBlockEntity extends BlockEntity implements TickableBlockEntity {

    private boolean isInitialized = false;
    protected @Nullable PartitionData data;

    /**
     * The travel speed in AU/tick.
     */
    public abstract double getTravelSpeed();

    protected StationEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void clientTick(ClientLevel level, long time, BlockState state, BlockPos pos) {

    }

    @Override
    public void serverTick(ServerLevel level, long time, BlockState state, BlockPos pos) {

    }

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public void firstTick(Level level, BlockState state, BlockPos pos) {
        if (level.isClientSide()) return;
        if (level instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension() == ModDimensions.SPACE) {
                data = SpacePartitionSavedData.get(serverLevel).getDataForBlockPos(pos);
                data.addEngine(pos);
            }
        }
        isInitialized = true;
    }
}
