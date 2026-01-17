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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.xevianlight.aphelion.block.entity.custom.ElectricArcFurnaceEntity;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ElectricArcFurnace extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ElectricArcFurnace(Properties properties) {
        super(properties);
    }

    public static final MapCodec<ElectricArcFurnace> CODEC = simpleCodec(ElectricArcFurnace::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public static Properties getProperties() {
        return Properties
                .of()
                .sound(SoundType.METAL)
                .destroyTime(2f)
                .explosionResistance(10f)
                .requiresCorrectToolForDrops();
    }

    public static Item.Properties getItemProperties() {
        return new Item.Properties();
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof ElectricArcFurnaceEntity electricArcFurnaceEntity) {
            serverPlayer.openMenu(new SimpleMenuProvider(electricArcFurnaceEntity, Component.literal("Electric Arc Furnace")), pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }



    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ElectricArcFurnaceEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity= level.getBlockEntity(pos);
            if (blockEntity instanceof ElectricArcFurnaceEntity electricArcFurnaceEntity) {
                electricArcFurnaceEntity.drops();
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
        slots.add(1);
        slots.add(2);
        slots.add(3);
        ElectricArcFurnaceEntity electricArcFurnaceEntity = ((ElectricArcFurnaceEntity) level.getBlockEntity(pos));
        return getRedstoneSignalFromItemHandler(electricArcFurnaceEntity.inventory, slots);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(blockEntityType, ModBlockEntities.ELECTRIC_ARC_FURNACE_ENTITY.get(), (level1, blockPos, blockState, electricArcFurnaceEntity) -> electricArcFurnaceEntity.tick(level1, blockPos, blockState));

    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(LIT, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }
}
