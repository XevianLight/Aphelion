package net.xevianlight.aphelion.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.core.init.ModBlocks;
import net.xevianlight.aphelion.core.init.ModItems;
import net.xevianlight.aphelion.util.ModTags;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        List<ItemLike> TEST_SMELTABLES = List.of(ModItems.TEST_ITEM, ModBlocks.TEST_BLOCK);


        //TEST BLOCK
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.TEST_BLOCK.get())
                .pattern("TTT")
                .pattern("TTT")
                .pattern("TTT")
                .define('T', ModItems.TEST_ITEM.get())
                .unlockedBy("has_test_item", has(ModItems.TEST_ITEM)).save(recipeOutput, "aphelion:test_shaped_recipe");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.TEST_ITEM.get(), 9)
                .requires(ModBlocks.TEST_BLOCK)
                .unlockedBy("has_test_block", has(ModBlocks.TEST_BLOCK)).save(recipeOutput, "aphelion:test_shapeless_recipe");

        //STEEL BLOCK
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BLOCK_STEEL.get())
                .pattern("III")
                .pattern("III")
                .pattern("III")
                .define('I', ModTags.Items.INGOT_STEEL)
                .unlockedBy("has_steel_ingot", has(ModItems.INGOT_ALUMINUM)).save(recipeOutput, "aphelion:steel_to_block");


        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.INGOT_STEEL.get(), 9)
                .requires(ModBlocks.BLOCK_STEEL)
                .unlockedBy("has_steel_block", has(ModBlocks.BLOCK_STEEL)).save(recipeOutput, "aphelion:steel_to_ingot");

        oreSmelting(recipeOutput, TEST_SMELTABLES, RecipeCategory.MISC, Items.IRON_INGOT, 0.25f, 200, "test");
    }


    protected static void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTime, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput, RecipeSerializer<T> pCookingSerializer, AbstractCookingRecipe.Factory<T> factory,
                                                                       List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(recipeOutput, Aphelion.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
}
