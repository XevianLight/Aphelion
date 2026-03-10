package net.xevianlight.aphelion.systems.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface ConveyorInput {
    void tick(ServerLevel level, BlockState state, BlockPos pos, Direction facingDirection, ConveyorNetwork graph);
}
