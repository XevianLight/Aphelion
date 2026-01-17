package net.xevianlight.aphelion.core.init;

import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.custom.BlockSteel;
import net.xevianlight.aphelion.block.custom.DimensionChangerBlock;
import net.xevianlight.aphelion.block.custom.ElectricArcFurnace;
import net.xevianlight.aphelion.block.custom.TestBlock;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Aphelion.MOD_ID);

    public static final  DeferredBlock<Block> TEST_BLOCK = BLOCKS.register("test_block", () -> new TestBlock(TestBlock.getProperties()));
    public static final  DeferredBlock<Block> BLOCK_STEEL = BLOCKS.register("block_steel", () -> new BlockSteel(BlockSteel.getProperties()));
    public static final  DeferredBlock<Block> DIMENSION_CHANGER = BLOCKS.register("dimension_changer", () -> new DimensionChangerBlock(DimensionChangerBlock.getProperties()));
    public static final  DeferredBlock<Block> ELECTRIC_ARC_FURNACE = BLOCKS.register("electric_arc_furnace", () -> new ElectricArcFurnace(ElectricArcFurnace.getProperties()));
}
