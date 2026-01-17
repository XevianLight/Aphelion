package net.xevianlight.aphelion.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.xevianlight.aphelion.block.entity.custom.TestBlockEntity;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TestBlock extends BaseEntityBlock {
    public TestBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    public static Properties getProperties() {
        return Properties
                .of()
                .sound(SoundType.ANVIL)
                .destroyTime(2f)
                .explosionResistance(10f)
                .requiresCorrectToolForDrops();
    }

    public static Item.Properties getItemProperties() {
        return new Item.Properties().stacksTo(3);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof TestBlockEntity testBlockEntity) {
            serverPlayer.openMenu(new SimpleMenuProvider(testBlockEntity, Component.literal("Test Block")), pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }



    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TestBlockEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity= level.getBlockEntity(pos);
            if (blockEntity instanceof TestBlockEntity testBlockEntity) {
                testBlockEntity.drops();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static int getRedstoneSignalFromItemHandler(@javax.annotation.Nullable ItemStackHandler itemStackHandler, List<Integer> slots) {
        if (itemStackHandler == null) {
            return 0;
        } else {
            int i = 0;
            float f = 0.0F;

            for(int slot : slots) {
                ItemStack itemstack = itemStackHandler.getStackInSlot(slot);
                if (!itemstack.isEmpty()) {
                    f += (float)itemstack.getCount() / (float)Math.min(itemStackHandler.getSlotLimit(slot), itemstack.getMaxStackSize());
                    ++i;
                }
            }

            f /= (float)slots.size();
            return Mth.floor(f * 14.0F) + (i > 0 ? 1 : 0);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return super.getSignal(state, level, pos, direction);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        List<Integer> slots = new ArrayList<>();
        slots.add(0);
        TestBlockEntity testBlockEntity = ((TestBlockEntity) level.getBlockEntity(pos));
        return getRedstoneSignalFromItemHandler(testBlockEntity.inventory, slots);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(blockEntityType, ModBlockEntities.TEST_BLOCK_ENTITY.get(), (level1, blockPos, blockState, testBlockEntity) -> testBlockEntity.tick(level1, blockPos, blockState));

    }
}
