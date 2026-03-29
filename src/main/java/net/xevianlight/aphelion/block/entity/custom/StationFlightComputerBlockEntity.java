package net.xevianlight.aphelion.block.entity.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xevianlight.aphelion.block.custom.base.TickableBlockEntity;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import net.xevianlight.aphelion.core.init.ModDimensions;
import net.xevianlight.aphelion.core.saveddata.SpacePartitionSavedData;
import net.xevianlight.aphelion.core.saveddata.types.PartitionData;
import org.jetbrains.annotations.Nullable;

public class StationFlightComputerBlockEntity extends BlockEntity implements TickableBlockEntity {
    public StationFlightComputerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.STATION_FLIGHT_COMPUTER_BLOCK_ENTITY.get(), pos, blockState);
    }

    protected PartitionData data;
    private boolean isInitialized = false;

    @Override
    public void clientTick(ClientLevel level, long time, BlockState state, BlockPos pos) {

    }

    @Override
    public void serverTick(ServerLevel level, long time, BlockState state, BlockPos pos) {
        if (data == null) return;
        data.setTraveling(true);
    }

    public @Nullable PartitionData getData() {
        return data;
    }

    @Override
    public void firstTick(Level level, BlockState state, BlockPos pos) {
        if (level.isClientSide()) return;
        if (level instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension() == ModDimensions.SPACE) {
                data = SpacePartitionSavedData.get(serverLevel).getDataForBlockPos(pos);

            }
        }
        isInitialized = true;
    }

    protected boolean setTraveling(boolean value) {
        if (data == null) return false;
        data.setTraveling(value);
        return true;
    }

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }
}
