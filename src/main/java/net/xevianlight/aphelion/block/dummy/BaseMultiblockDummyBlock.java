package net.xevianlight.aphelion.block.dummy;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.xevianlight.aphelion.util.IMultiblockController;
import net.xevianlight.aphelion.util.IMultiblockPart;
import org.jetbrains.annotations.Nullable;

public class BaseMultiblockDummyBlock extends BaseEntityBlock {

    public static final MapCodec<BaseMultiblockDummyBlock> CODEC = simpleCodec(BaseMultiblockDummyBlock::new);

    public BaseMultiblockDummyBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return null;
    }

    public static Item.Properties getItemProperties() {
        return new Item.Properties();
    }

    public static Properties getProperties() {
        return Properties
                .of()
                .sound(SoundType.NETHERITE_BLOCK)
                .destroyTime(2f)
                .explosionResistance(10f)
                .requiresCorrectToolForDrops();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }


    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    private void pingNearbyController(Level level, BlockPos pos) {
        int r = 5;
        BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();

        for (int dx=-r; dx<=r; dx++)
            for (int dy=-r; dy<=r; dy++)
                for (int dz=-r; dz<=r; dz++) {
                    mp.set(pos.getX()+dx, pos.getY()+dy, pos.getZ()+dz);
                    BlockEntity be = level.getBlockEntity(mp);
                    if (be instanceof IMultiblockController controller) {
                            controller.markDirty();
                    }
                }
    }

//    @Override
//    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {
//        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
//            if (level.getBlockEntity(pos) instanceof IMultiblockPart multiblockPartEntity) {
////            if (multiblockPartEntity.getControllerPos() != null) {
//                if (level.getBlockEntity(multiblockPartEntity.getControllerPos()) instanceof IMultiblockController controller)
//                    serverPlayer.openMenu(new SimpleMenuProvider((MenuConstructor) level.getBlockEntity(multiblockPartEntity.getControllerPos()), Component.literal(controller.getMenuTitle())), multiblockPartEntity.getControllerPos());
////            } else {
////                if (level.getBlockEntity(pos) instanceof IMultiblockPart mbp) {
////                    level.setBlock(pos, mbp.getMimicing(), UPDATE_ALL);
////                } else {
////                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
////                }
////            }
//                return InteractionResult.sidedSuccess(level.isClientSide);
//            }
//        }
//
//        return InteractionResult.FAIL;
//    }


    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (level.getBlockEntity(pos) instanceof IMultiblockPart mbPartEntity) {
                if (mbPartEntity.getControllerPos() != null) {
                    if (level.getBlockEntity(mbPartEntity.getControllerPos()) instanceof IMultiblockController controller) {
                        serverPlayer.openMenu(new SimpleMenuProvider((MenuConstructor) level.getBlockEntity(mbPartEntity.getControllerPos()), Component.literal(controller.getMenuTitle())), mbPartEntity.getControllerPos());
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                } else {
                    if (level.getBlockEntity(pos) instanceof IMultiblockPart mbp) {
                        level.setBlock(pos, mbp.getMimicing(), UPDATE_ALL);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
                    }
                    return InteractionResult.FAIL;
                }
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hit) {
        // Always handle dummy interaction, even with an item in hand
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof IMultiblockPart part) {

            if (part.getControllerPos() != null) {
                BlockEntity cbe = level.getBlockEntity(part.getControllerPos());
                if (cbe instanceof IMultiblockController controller) {
                    serverPlayer.openMenu(
                            new SimpleMenuProvider((MenuConstructor) cbe, Component.literal(controller.getMenuTitle())),
                            part.getControllerPos()
                    );
                }
            } else {
                level.setBlock(pos, part.getMimicing() != null ? part.getMimicing() : Blocks.AIR.defaultBlockState(), UPDATE_ALL);
            }

            return ItemInteractionResult.CONSUME; // <- prevents item use/placement
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {

    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof IMultiblockPart mbPart)
                mbPart.onDummyBroken();
        }
//        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
