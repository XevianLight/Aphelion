package net.xevianlight.aphelion.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.core.init.ModItems;
import net.xevianlight.aphelion.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, Aphelion.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Items.TEST_TAG)
                .add(ModItems.TEST_ITEM.get());

        tag(ModTags.Items.INGOTS)
                .add(ModItems.INGOT_ALUMINUM.get())
                .add(ModItems.INGOT_STEEL.get())
                .add(ModItems.INGOT_TITANIUM.get())
                .add(ModItems.INGOT_URANIUM.get())
                .add(ModItems.INGOT_COBALT.get())
                .add(ModItems.INGOT_TUNGSTEN.get())
                .add(ModItems.INGOT_NEODYMIUM.get())
                .add(ModItems.INGOT_IRIDIUM.get());

        tag(ModTags.Items.INGOT_ALUMINUM)
                .add(ModItems.INGOT_ALUMINUM.get());

        tag(ModTags.Items.INGOT_STEEL)
                .add(ModItems.INGOT_STEEL.get());

        tag(ModTags.Items.INGOT_TITANIUM)
                .add(ModItems.INGOT_TITANIUM.get());

        tag(ModTags.Items.INGOT_URANIUM)
                .add(ModItems.INGOT_URANIUM.get());

        tag(ModTags.Items.INGOT_COBALT)
                .add(ModItems.INGOT_COBALT.get());

        tag(ModTags.Items.INGOT_TUNGSTEN)
                .add(ModItems.INGOT_TUNGSTEN.get());

        tag(ModTags.Items.INGOT_NEODYMIUM)
                .add(ModItems.INGOT_NEODYMIUM.get());

        tag(ModTags.Items.INGOT_IRIDIUM)
                .add(ModItems.INGOT_IRIDIUM.get());


        tag(ModTags.Items.STORAGE_BLOCKS)
                .add(ModItems.BLOCK_STEEL.get());

        tag(ModTags.Items.STORAGE_BLOCKS_STEEL)
                .add(ModItems.BLOCK_STEEL.get());
    }
}
