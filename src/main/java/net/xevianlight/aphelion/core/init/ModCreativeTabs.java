package net.xevianlight.aphelion.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.fluid.ModFluids;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Aphelion.MOD_ID);

    public static final Supplier<CreativeModeTab> APHELION_ITEMS_TAB = CREATIVE_MODE_TAB.register("aphelion_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.TEST_ITEM.get()))
                    .title(Component.translatable("creativetab.aphelion.aphelion_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.TEST_ITEM);
                        output.accept(ModItems.INGOT_ALUMINUM);
                        output.accept(ModItems.INGOT_STEEL);
                        output.accept(ModItems.INGOT_TITANIUM);
                        output.accept(ModItems.INGOT_URANIUM);
                        output.accept(ModItems.INGOT_COBALT);
                        output.accept(ModItems.INGOT_TUNGSTEN);
                        output.accept(ModItems.INGOT_NEODYMIUM);
                        output.accept(ModItems.INGOT_IRIDIUM);
                        output.accept(ModFluids.OIL_BUCKET);
                        output.accept(ModItems.MUSIC_DISC_BIT_SHIFT);
                    }).build());

    public static final Supplier<CreativeModeTab> APHELION_BLOCKS_TAB = CREATIVE_MODE_TAB.register("aphelion_blocks_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.TEST_BLOCK.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "aphelion_items_tab"))
                    .title(Component.translatable("creativetab.aphelion.aphelion_blocks"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.TEST_BLOCK);
                        output.accept(ModItems.ELECTRIC_ARC_FURNACE);
                        output.accept(ModItems.BLOCK_STEEL);
                    }).build());
}
