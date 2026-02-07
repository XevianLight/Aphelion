package net.xevianlight.aphelion.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.core.init.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Aphelion.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.TEST_BLOCK);

        horizontalBlock(ModBlocks.ELECTRIC_ARC_FURNACE.get(), models().orientable("aphelion:electric_arc_furnace",
                mcLoc("block/blast_furnace_side"),
                modLoc("block/electric_arc_furnace_front"),
                mcLoc("block/blast_furnace_top")));
        blockItem(ModBlocks.ELECTRIC_ARC_FURNACE);
        blockItem(ModBlocks.VACUUM_ARC_FURNACE_CONTROLLER);

//        horizontalBlock(ModBlocks.ROCKET_ASSEMBLER_BLOCK.get(), models().orientable("aphelion:rocket_assembler_block",
//                modLoc("block/test_block"),
//                mcLoc("block/furnace_front"),
//                modLoc("block/test_block")));
        blockItem(ModBlocks.ROCKET_ASSEMBLER_BLOCK);

        blockWithItem(ModBlocks.BLOCK_STEEL);
        blockWithItem(ModBlocks.DIMENSION_CHANGER);

//        this is already defined ourselves
//        blockItem(ModBlocks.LAUNCH_PAD);

        blockItem(ModBlocks.ARC_FURNACE_CASING_BLOCK);
        blockWithItem(ModBlocks.OXYGEN_TEST_BLOCK);
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void blockItem(DeferredBlock<Block> deferredBlock) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("aphelion:block/" + deferredBlock.getId().getPath()));
    }
}
