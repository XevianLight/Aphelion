package net.xevianlight.aphelion.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.fluid.ModFluids;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModFluidTagsProvider extends FluidTagsProvider {

    public ModFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, Aphelion.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
//        tag(FluidTags.LAVA)
//                .add(ModFluids.SOURCE_OIL_FLUID.get())
//                .add(ModFluids.FLOWING_OIL_FLUID.get());
    }
}
