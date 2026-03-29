package net.xevianlight.aphelion.core.init;

import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.custom.*;
import net.xevianlight.aphelion.block.dummy.VAFMultiblockDummyBlock;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Aphelion.MOD_ID);

    public static final DeferredBlock<Block> TEST_BLOCK = BLOCKS.register("test_block", () -> new TestBlock(TestBlock.getProperties()));
    public static final DeferredBlock<Block> BLOCK_STEEL = BLOCKS.register("block_steel", () -> new BlockSteel(BlockSteel.getProperties()));
    public static final DeferredBlock<Block> LAUNCH_PAD = BLOCKS.register("launch_pad", () -> new LaunchPad(LaunchPad.getProperties()));
    public static final DeferredBlock<Block> DIMENSION_CHANGER = BLOCKS.register("dimension_changer", () -> new DimensionChangerBlock(DimensionChangerBlock.getProperties()));
    public static final DeferredBlock<Block> ELECTRIC_ARC_FURNACE = BLOCKS.register("electric_arc_furnace", () -> new ElectricArcFurnace(ElectricArcFurnace.getProperties()));
    public static final DeferredBlock<Block> ARC_FURNACE_CASING_BLOCK = BLOCKS.register("arc_furnace_casing", () -> new ArcFurnaceCasingBlock(ArcFurnaceCasingBlock.getProperties()));
    public static final DeferredBlock<Block> VACUUM_ARC_FURNACE_CONTROLLER = BLOCKS.register("vacuum_arc_furnace_controller", () -> new VacuumArcFurnaceController(VacuumArcFurnaceController.getProperties()));
    public static final DeferredBlock<Block> VAF_MULTIBLOCK_DUMMY_BLOCK = BLOCKS.register("vaf_dummy_block", () -> new VAFMultiblockDummyBlock(VAFMultiblockDummyBlock.getProperties()));
    public static final DeferredBlock<Block> OXYGEN_TEST_BLOCK = BLOCKS.register("oxygen_test_block", () -> new OxygenTestBlock(OxygenTestBlock.getProperties()));
    public static final DeferredBlock<Block> ROCKET_ASSEMBLER = BLOCKS.register("rocket_assembler", () -> new RocketAssembler(RocketAssembler.getProperties()));
    public static final DeferredBlock<Block> ROCKET_SEAT = BLOCKS.register("rocket_seat", () -> new RocketSeat(RocketSeat.getProperties()));
    public static final DeferredBlock<Block> STATION_ROCKET_ENGINE = BLOCKS.register("station_rocket_engine", () -> new StationRocketEngineBlock(StationRocketEngineBlock.getProperties()));
    public static final DeferredBlock<Block> BASIC_ROCKET_FUEL_TANK = BLOCKS.register("basic_rocket_fuel_tank", () -> new BasicRocketFuelTank(BasicRocketFuelTank.getProperties()));
    public static final DeferredBlock<Block> BASIC_ROCKET_CONTAINER = BLOCKS.register("basic_rocket_container", () -> new BasicRocketContainer(BasicRocketContainer.getProperties()));
    public static final DeferredBlock<Block> STATION_FLIGHT_COMPUTER_BLOCK = BLOCKS.register("station_flight_computer", () -> new StationFlightComputerBlock(StationFlightComputerBlock.getProperties()));
}
