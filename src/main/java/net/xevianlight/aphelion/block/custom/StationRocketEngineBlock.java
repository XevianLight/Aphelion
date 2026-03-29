package net.xevianlight.aphelion.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.custom.base.StationEngineBlock;
import net.xevianlight.aphelion.block.entity.custom.StationRocketEngineBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StationRocketEngineBlock extends StationEngineBlock {
    public StationRocketEngineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new StationRocketEngineBlockEntity(blockPos, blockState);
    }

    @Override
    public @NotNull ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {

        // Only intercept on client if holding a fluid container, otherwise let block placement through
        if (level.isClientSide) {
            return FluidUtil.getFluidHandler(stack).isPresent()
                    ? ItemInteractionResult.SUCCESS
                    : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof StationRocketEngineBlockEntity engineBE)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        IFluidHandler tankHandler = level.getCapability(
                Capabilities.FluidHandler.BLOCK, pos, state, be, null
        );

        if (tankHandler == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        FluidStack fluidBeforeInteraction = FluidUtil.getFluidContained(player.getItemInHand(hand))
                .orElse(FluidStack.EMPTY);

        boolean success = FluidUtil.interactWithFluidHandler(player, hand, tankHandler);

        if (success) {
            FluidStack tankFluid = tankHandler.getFluidInTank(0);
            FluidStack relevantFluid = fluidBeforeInteraction.isEmpty() ? tankFluid : fluidBeforeInteraction;

            Aphelion.LOGGER.info("fluidBeforeInteraction: {}", fluidBeforeInteraction);
            Aphelion.LOGGER.info("tankFluid: {}", tankFluid);
            Aphelion.LOGGER.info("relevantFluid: {}", relevantFluid);

            if (!relevantFluid.isEmpty()) {
                FluidType fluidType = relevantFluid.getFluid().getFluidType();
                SoundEvent sound = fluidBeforeInteraction.isEmpty()
                        ? fluidType.getSound(SoundActions.BUCKET_FILL)
                        : fluidType.getSound(SoundActions.BUCKET_EMPTY);

                Aphelion.LOGGER.info("fluidType: {}", fluidType);
                Aphelion.LOGGER.info("sound: {}", sound);

                if (sound != null) {
                    Aphelion.LOGGER.info("Playing sound!");
                    level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }

            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
