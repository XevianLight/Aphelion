package net.xevianlight.aphelion.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.xevianlight.aphelion.core.init.ModBlocks;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    protected ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.TEST_BLOCK.get());
        dropSelf(ModBlocks.BLOCK_STEEL.get());
        dropSelf(ModBlocks.DIMENSION_CHANGER.get());
        dropSelf(ModBlocks.ELECTRIC_ARC_FURNACE.get());
        dropSelf(ModBlocks.ARC_FURNACE_CASING_BLOCK.get());
        dropSelf(ModBlocks.VACUUM_ARC_FURNACE_CONTROLLER.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
