package net.xevianlight.aphelion.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.core.init.ModBlocks;
import net.xevianlight.aphelion.core.init.ModItems;

import java.util.function.Supplier;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, Aphelion.MOD_ID);

    public static final Supplier<FlowingFluid> SOURCE_OIL_FLUID = FLUIDS.register("oil",
            () -> new BaseFlowingFluid.Source(ModFluids.OIL_PROPERTIES));
    public static final Supplier<FlowingFluid> FLOWING_OIL_FLUID = FLUIDS.register("flowing_oil",
            () -> new BaseFlowingFluid.Flowing(ModFluids.OIL_PROPERTIES));

    public static final DeferredBlock<LiquidBlock> OIL_BLOCK = ModBlocks.BLOCKS.register("oil",
        () -> new LiquidBlock(ModFluids.SOURCE_OIL_FLUID.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredItem<Item> OIL_BUCKET = ModItems.ITEMS.registerItem("oil_bucket",
            properties -> new BucketItem(ModFluids.SOURCE_OIL_FLUID.get(), properties.stacksTo(1).craftRemainder(Items.BUCKET)));

    public static final BaseFlowingFluid.Properties OIL_PROPERTIES = new BaseFlowingFluid.Properties(
            ModFluidTypes.OIL_FLUID_TYPE, SOURCE_OIL_FLUID, FLOWING_OIL_FLUID)
            .slopeFindDistance(2).levelDecreasePerBlock(2).tickRate(10)
            .block(ModFluids.OIL_BLOCK).bucket(ModFluids.OIL_BUCKET);

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
