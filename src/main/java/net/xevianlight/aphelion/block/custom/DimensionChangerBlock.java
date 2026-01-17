package net.xevianlight.aphelion.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.xevianlight.aphelion.block.entity.custom.DimensionChangerBlockEntity;
import org.jetbrains.annotations.Nullable;

public class DimensionChangerBlock extends BaseEntityBlock {

    public DimensionChangerBlock(Properties properties) {
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


        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof DimensionChangerBlockEntity dimensionChangerBlockEntity) {
            ServerLevel sourceDim = serverPlayer.serverLevel();
            ServerLevel targetDim = serverPlayer.server.getLevel(Level.NETHER);
            serverPlayer.changeDimension(new DimensionTransition(
                    targetDim,
                    serverPlayer.position().multiply(
                            sourceDim.dimensionType().coordinateScale() / targetDim.dimensionType().coordinateScale(),
                            1,
                            sourceDim.dimensionType().coordinateScale() / targetDim.dimensionType().coordinateScale()),
                    Vec3.ZERO, serverPlayer.getYRot(),
                    serverPlayer.getXRot(),
                    DimensionTransition.PLAY_PORTAL_SOUND
            ));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DimensionChangerBlockEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity= level.getBlockEntity(pos);
            if (blockEntity instanceof DimensionChangerBlockEntity dimensionChangerBlockEntity) {
                dimensionChangerBlockEntity.drops();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
