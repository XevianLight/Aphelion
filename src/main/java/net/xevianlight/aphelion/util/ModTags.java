package net.xevianlight.aphelion.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.xevianlight.aphelion.Aphelion;

public class ModTags {
    public static class Blocks {
        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, name));
        }

        public static final TagKey<Block> STORAGE_BLOCKS = commonTag("storage_blocks");
        public static final TagKey<Block> STORAGE_BLOCKS_STEEL = commonTag("storage_blocks/steel");

        public static final TagKey<Block> LAUNCH_PAD = createTag("launch_pad");

        private static TagKey<Block> commonTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }

    public static class Items {
        public static final TagKey<Item> TEST_TAG = createTag("test_tag");
        public static final TagKey<Item> INGOTS = commonTag("ingots");
        public static final TagKey<Item> STORAGE_BLOCKS = commonTag("storage_blocks");
        public static final TagKey<Item> STORAGE_BLOCKS_STEEL = commonTag("storage_blocks/steel");
        public static final TagKey<Item> INGOT_ALUMINUM = commonTag("ingots/aluminum");
        public static final TagKey<Item> INGOT_STEEL = commonTag("ingots/steel");
        public static final TagKey<Item> INGOT_TITANIUM = commonTag("ingots/titanium");
        public static final TagKey<Item> INGOT_URANIUM = commonTag("ingots/uranium");
        public static final TagKey<Item> INGOT_COBALT = commonTag("ingots/cobalt");
        public static final TagKey<Item> INGOT_TUNGSTEN = commonTag("ingots/tungsten");
        public static final TagKey<Item> INGOT_NEODYMIUM = commonTag("ingots/neodymium");
        public static final TagKey<Item> INGOT_IRIDIUM = commonTag("ingots/iridium");

        public static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, name));
        }

        private static TagKey<Item> commonTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }

    }
}
