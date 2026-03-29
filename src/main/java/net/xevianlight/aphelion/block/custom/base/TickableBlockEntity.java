package net.xevianlight.aphelion.block.custom.base;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface TickableBlockEntity {

    /**
     * Runs on both the client and server once per tick.
     *
     * <p>This is intended for logic that is common to both sides. Side-specific logic
     * should go in {@link #clientTick(ClientLevel, long, BlockState, BlockPos)} or
     * {@link #serverTick(ServerLevel, long, BlockState, BlockPos)}.</p>
     *
     * @param level the current level
     * @param time a deterministic per-position tick time (see ticker implementation)
     * @param state the current block state
     * @param pos the world position of the block entity
     */
    default void tick (Level level, long time, BlockState state, BlockPos pos) {};

    /**
     * Runs on the client only once per tick.
     *
     * <p>Use this for client-side visual updates, particles, sounds, animation state,
     * or other logic that must not run on the logical server.</p>
     *
     * @param level the client level
     * @param time a deterministic per-position tick time (see ticker implementation)
     * @param state the current block state
     * @param pos the world position of the block entity
     */
    void clientTick(ClientLevel level, long time, BlockState state, BlockPos pos);

    /**
     * Runs on the server only once per tick.
     *
     * <p>Use this for authoritative game logic such as inventory processing, energy
     * generation/consumption, entity spawning, saving state, and network sync triggers.</p>
     *
     * @param level the server level
     * @param time a deterministic per-position tick time (see ticker implementation)
     * @param state the current block state
     * @param pos the world position of the block entity
     */
    void serverTick(ServerLevel level, long time, BlockState state, BlockPos pos);

    /**
     * Returns whether this object has completed its initialization logic.
     *
     * <p>If this method returns {@code false}, {@link #firstTick(Level, BlockState, BlockPos)}
     * will be invoked at the start of each tick on both the client and server until
     * initialization is complete.</p>
     *
     * <p>Implementations should return {@code true} once initialization has finished
     * to prevent {@code firstTick} from running again.</p>
     *
     * <p>Returns {@code true} if not implemented.</p>
     *
     * @return {@code true} if initialization has completed, {@code false} otherwise
     */
    default boolean isInitialized() {
        return true;
    }

    /**
     * Performs initialization logic for this object.
     *
     * <p>This method is called at the start of each tick on both the client and server
     * whenever {@link #isInitialized()} returns {@code false}. It will continue to be
     * invoked every tick until initialization is complete.</p>
     *
     * <p>Implementations should perform any required setup and ensure that
     * {@code isInitialized()} returns {@code true} afterward.</p>
     *
     * <p>Will never run if {@link #isInitialized()} is not implemented.</p>
     *
     * @param level the level the block entity exists in
     * @param state the current block state
     * @param pos the world position of the block entity
     */
    void firstTick(Level level, BlockState state, BlockPos pos);

    default void onRemoved() {}
}
