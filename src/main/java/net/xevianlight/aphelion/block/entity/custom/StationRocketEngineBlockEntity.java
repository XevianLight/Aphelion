package net.xevianlight.aphelion.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.xevianlight.aphelion.block.entity.custom.base.StationEngineBlockEntity;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import net.xevianlight.aphelion.core.init.ModFluidTags;

public class StationRocketEngineBlockEntity extends StationEngineBlockEntity {

    /// Seconds to travel 1 AU
    private final double SECONDS_PER_AU = 60;
    /// AU per tick
    private final double SPEED = 1/(SECONDS_PER_AU*20);
    ///  Fuel consumption per tick in millibuckets
    private static final int FUEL_CONSUMPTION = 10;

    private FluidTank tank = new FluidTank(
            2000,
            fluidStack -> fluidStack.is(ModFluidTags.ROCKET_FUEL)
    );

    @Override
    public double getTravelSpeed() {
        return SPEED;
    }

    public IFluidHandler getFluidStorage(Direction direction) {
        return tank;
    }

    public StationRocketEngineBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.STATION_ROCKET_ENGINE_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public void serverTick(ServerLevel level, long time, BlockState state, BlockPos pos) {
        super.serverTick(level, time, state, pos);
        burn();
    }

    private void burn() {
        if (data == null)
            return;

        if (data.getDestination() != null && data.isTraveling()) {
            if (!tank.isEmpty() && tank.getFluid().is(ModFluidTags.ROCKET_FUEL) && tank.getFluidAmount() >= FUEL_CONSUMPTION) { // has enough fuel?
                if (data.travel(getTravelSpeed()))
                    tank.drain(FUEL_CONSUMPTION, IFluidHandler.FluidAction.EXECUTE);
            } else {
                // not enough fuel
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fluid", tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("fluid")) {
            tank.readFromNBT(registries, tag.getCompound("fluid"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
