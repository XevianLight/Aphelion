package net.xevianlight.aphelion.systems.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.xevianlight.aphelion.block.entity.custom.PipeTestBlockEntity;

public class ConveyorAttachment {
    ConveyorAttachment(ServerLevel level, BlockPos pos, Direction facingDirection) {}

    //TODO: put in the right interface for a render function
    void render() {};

    public void tick(ServerLevel level, BlockState state, BlockPos pos, Direction facingDirection, ConveyorNetwork network) {}
}
