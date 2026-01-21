package net.xevianlight.aphelion.core.init;

import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.custom.*;
import net.xevianlight.aphelion.block.dummy.VAFMultiblockDummyBlock;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Aphelion.MOD_ID);

    public static final  DeferredBlock<Block> TEST_BLOCK = BLOCKS.register("test_block", () -> new TestBlock(TestBlock.getProperties()));
    public static final  DeferredBlock<Block> BLOCK_STEEL = BLOCKS.register("block_steel", () -> new BlockSteel(BlockSteel.getProperties()));
    public static final  DeferredBlock<Block> DIMENSION_CHANGER = BLOCKS.register("dimension_changer", () -> new DimensionChangerBlock(DimensionChangerBlock.getProperties()));
    public static final  DeferredBlock<Block> ELECTRIC_ARC_FURNACE = BLOCKS.register("electric_arc_furnace", () -> new ElectricArcFurnace(ElectricArcFurnace.getProperties()));
    public static final  DeferredBlock<Block> ARC_FURNACE_CASING_BLOCK = BLOCKS.register("arc_furnace_casing", () -> new ArcFurnaceCasingBlock(ArcFurnaceCasingBlock.getProperties()));
    public static final  DeferredBlock<Block> VACUUM_ARC_FURNACE_CONTROLLER = BLOCKS.register("vacuum_arc_furnace_controller", () -> new VacuumArcFurnaceController(VacuumArcFurnaceController.getProperties()));
    public static final  DeferredBlock<Block> VAF_MULTIBLOCK_DUMMY_BLOCK = BLOCKS.register("vaf_dummy_block", () -> new VAFMultiblockDummyBlock(VAFMultiblockDummyBlock.getProperties()));
}
