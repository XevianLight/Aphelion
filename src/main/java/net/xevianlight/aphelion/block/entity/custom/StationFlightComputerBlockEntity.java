package net.xevianlight.aphelion.block.entity.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xevianlight.aphelion.block.custom.base.TickableBlockEntity;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import net.xevianlight.aphelion.core.init.ModDimensions;
import net.xevianlight.aphelion.core.saveddata.SpacePartitionSavedData;
import net.xevianlight.aphelion.core.saveddata.types.PartitionData;
import net.xevianlight.aphelion.screen.StationFlightComputerMenu;
import org.jetbrains.annotations.Nullable;

public class StationFlightComputerBlockEntity extends BlockEntity implements TickableBlockEntity, MenuProvider {

    protected PartitionData data;
    private boolean isInitialized = false;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            if (data == null) return 0;
            return switch (index) {
                case StationFlightComputerMenu.DATA_TRAVELING    -> data.isTraveling() ? 1 : 0;
                case StationFlightComputerMenu.DATA_ENGINE_COUNT -> data.getEngines().size();
                case StationFlightComputerMenu.DATA_PAD_COUNT    -> data.getLandingPadControllers().size();
                default -> 0;
            };
        }
        // set() is intentionally a no-op: client writes go through explicit network packets, not ContainerData
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return StationFlightComputerMenu.DATA_COUNT; }
    };

    public StationFlightComputerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.STATION_FLIGHT_COMPUTER_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public void clientTick(ClientLevel level, long time, BlockState state, BlockPos pos) {}

    @Override
    public void serverTick(ServerLevel level, long time, BlockState state, BlockPos pos) {}

    public @Nullable PartitionData getData() { return data; }

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

    public boolean setTraveling(boolean value) {
        if (data == null) return false;
        data.setTraveling(value);
        return true;
    }

    public void setDestination(@Nullable ResourceLocation destination) {
        if (data == null) return;
        data.setDestination(destination);
    }

    public ContainerData getContainerData() { return containerData; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.aphelion.station_flight_computer");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new StationFlightComputerMenu(windowId, inventory, this, containerData);
    }

    @Override
    public boolean isInitialized() { return isInitialized; }
}
