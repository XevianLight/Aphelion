package net.xevianlight.aphelion.block.custom.base;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface TickableBlockEntity {

    /**
     * Runs on both the client AND server.
     * @param level
     * @param time
     * @param state
     * @param pos
     */
    default void tick (Level level, long time, BlockState state, BlockPos pos) {};

    /**
     * Runs on the client only
     * @param level
     * @param time
     * @param state
     * @param pos
     */
    void clientTick(ClientLevel level, long time, BlockState state, BlockPos pos);

    /**
     * Runs on the server only
     * @param level
     * @param time
     * @param state
     * @param pos
     */
    void serverTick(ServerLevel level, long time, BlockState state, BlockPos pos);

    default boolean isInitialized() {
        return true;
    };

    /**
     * Runs on client AND server, once only.
     * @param level
     * @param state
     * @param pos
     */
    void firstTick(Level level, BlockState state, BlockPos pos);

    default void onRemoved() {}
}
