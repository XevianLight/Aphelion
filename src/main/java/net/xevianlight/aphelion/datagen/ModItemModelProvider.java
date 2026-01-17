package net.xevianlight.aphelion.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.core.init.ModItems;
import net.xevianlight.aphelion.fluid.ModFluids;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Aphelion.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.TEST_ITEM.get());
        basicItem(ModItems.INGOT_ALUMINUM.get());
        basicItem(ModItems.INGOT_STEEL.get());
        basicItem(ModItems.INGOT_TITANIUM.get());
        basicItem(ModItems.INGOT_URANIUM.get());
        basicItem(ModItems.INGOT_COBALT.get());
        basicItem(ModItems.INGOT_TUNGSTEN.get());
        basicItem(ModItems.INGOT_NEODYMIUM.get());
        basicItem(ModItems.INGOT_IRIDIUM.get());

        basicItem(ModFluids.OIL_BUCKET.get());
        basicItem(ModItems.MUSIC_DISC_BIT_SHIFT.get());
    }
}
