package net.xevianlight.aphelion.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.core.init.ModBlocks;
import net.xevianlight.aphelion.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Aphelion.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.TEST_BLOCK.get())
                .add(ModBlocks.ELECTRIC_ARC_FURNACE.get())
                .add(ModBlocks.BLOCK_STEEL.get())
                .add(ModBlocks.ARC_FURNACE_CASING_BLOCK.get())
                .add(ModBlocks.VACUUM_ARC_FURNACE_CONTROLLER.get())
                .add(ModBlocks.VAF_MULTIBLOCK_DUMMY_BLOCK.get());

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.TEST_BLOCK.get())
                .add(ModBlocks.ELECTRIC_ARC_FURNACE.get())
                .add(ModBlocks.BLOCK_STEEL.get())
                .add(ModBlocks.ARC_FURNACE_CASING_BLOCK.get())
                .add(ModBlocks.VACUUM_ARC_FURNACE_CONTROLLER.get())
                .add(ModBlocks.VAF_MULTIBLOCK_DUMMY_BLOCK.get());

        tag(ModTags.Blocks.STORAGE_BLOCKS_STEEL)
                .add(ModBlocks.BLOCK_STEEL.get());

        tag(ModTags.Blocks.STORAGE_BLOCKS)
                .add(ModBlocks.BLOCK_STEEL.get());

        tag(ModTags.Blocks.LAUNCH_PAD)
                .add(ModBlocks.LAUNCH_PAD.get());

    }
}
