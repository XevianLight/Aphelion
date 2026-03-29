package net.xevianlight.aphelion.block.custom.base;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BasicEntityBlock extends BaseEntityBlock {

    private final boolean shouldTick;
    private BlockEntityType<?> entity;

    protected BasicEntityBlock(Properties properties, boolean shouldTick) {
        super(properties);
        this.shouldTick = shouldTick;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    public static Properties getProperties() {
        return Properties.of();
    }

    public static Item.Properties getItemProperties() {
        return new Item.Properties();
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return !shouldTick ? null : (entityLevel, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof TickableBlockEntity tickable) {
                if (!tickable.isInitialized()) tickable.firstTick(entityLevel, blockState, pos);

                long time = entityLevel.getGameTime() - pos.hashCode();
                tickable.tick(entityLevel, time, blockState, pos);

                if (entityLevel.isClientSide()) {
                    tickable.clientTick((ClientLevel) entityLevel, time, blockState, pos);
                } else {
                    tickable.serverTick((ServerLevel) entityLevel, time, blockState, pos);
                }
            }
        };
    }

    @Override
    protected void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof TickableBlockEntity tickable) {
                tickable.onRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
