package net.xevianlight.aphelion.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import net.xevianlight.aphelion.block.custom.base.BasicHorizontalEntityBlock;
import net.xevianlight.aphelion.block.entity.custom.StationFlightComputerBlockEntity;
import net.xevianlight.aphelion.core.saveddata.types.PartitionData;
import net.xevianlight.aphelion.network.packet.AvailableDestinationsPayload;
import net.xevianlight.aphelion.network.packet.PlanetInfo;
import net.xevianlight.aphelion.planet.PlanetCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class StationFlightComputerBlock extends BasicHorizontalEntityBlock {

    public static final MapCodec<StationFlightComputerBlock> CODEC = simpleCodec(StationFlightComputerBlock::new);

    public StationFlightComputerBlock(Properties properties) {
        super(properties, true);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new StationFlightComputerBlockEntity(blockPos, blockState);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level,
                                                        @NotNull BlockPos pos, @NotNull Player player,
                                                        @NotNull BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (level.getBlockEntity(pos) instanceof StationFlightComputerBlockEntity be) {
                List<PlanetInfo> planets = PlanetCache.PLANETS.entrySet().stream()
                        .map(e -> new PlanetInfo(
                                e.getKey(),
                                e.getValue().orbit().location(),
                                e.getValue().orbitDistance(),
                                e.getValue().parentPlanet().map(k -> k.location())))
                        .collect(Collectors.toList());
                // Send planet list before opening the menu so DestinationClientCache is populated when the screen opens.
                PacketDistributor.sendToPlayer(serverPlayer, new AvailableDestinationsPayload(planets));
                serverPlayer.openMenu(be, be.getBlockPos());
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        // Breaking the flight computer aborts travel — no computer, no navigation.
        if (level.getBlockEntity(pos) instanceof StationFlightComputerBlockEntity computerBE) {
            PartitionData data = computerBE.getData();
            if (data != null) {
                data.setTraveling(false);
            }
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }
}
