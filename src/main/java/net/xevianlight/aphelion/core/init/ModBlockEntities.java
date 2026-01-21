package net.xevianlight.aphelion.core.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.dummy.entity.VAFMultiblockDummyBlockEntity;
import net.xevianlight.aphelion.block.entity.custom.*;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Aphelion.MOD_ID);

    public static final Supplier<BlockEntityType<TestBlockEntity>> TEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("test_block_entity", () -> BlockEntityType.Builder.of(
                    TestBlockEntity::new, ModBlocks.TEST_BLOCK.get()).build(null)
            );

    public static final Supplier<BlockEntityType<DimensionChangerBlockEntity>> DIMENSION_CHANGER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("dimension_changer_block_entity", () -> BlockEntityType.Builder.of(
                    DimensionChangerBlockEntity::new, ModBlocks.DIMENSION_CHANGER.get()).build(null)
            );

    public static final Supplier<BlockEntityType<ElectricArcFurnaceEntity>> ELECTRIC_ARC_FURNACE_ENTITY =
            BLOCK_ENTITIES.register("electric_arc_furnace_block_entity", () -> BlockEntityType.Builder.of(
                    ElectricArcFurnaceEntity::new, ModBlocks.ELECTRIC_ARC_FURNACE.get()).build(null)
            );

    public static final Supplier<BlockEntityType<EAFPartEntity>> EAF_PART_ENTITY =
            BLOCK_ENTITIES.register("eaf_part_entity", () -> BlockEntityType.Builder.of(
                    EAFPartEntity::new, ModBlocks.ARC_FURNACE_CASING_BLOCK.get()).build(null)
            );

    public static final Supplier<BlockEntityType<VacuumArcFurnaceControllerEntity>> VACUUM_ARC_FURNACE_ENTITY =
            BLOCK_ENTITIES.register("vacuum_arc_furnace_controller_entity", () -> BlockEntityType.Builder.of(
                    VacuumArcFurnaceControllerEntity::new, ModBlocks.VACUUM_ARC_FURNACE_CONTROLLER.get()).build(null)
            );

    public static final Supplier<BlockEntityType<VAFMultiblockDummyBlockEntity>> VAF_MULTIBLOCK_DUMMY_ENTITY =
            BLOCK_ENTITIES.register("vaf_multiblock_dummy_entity", () -> BlockEntityType.Builder.of(
                    VAFMultiblockDummyBlockEntity::new, ModBlocks.VAF_MULTIBLOCK_DUMMY_BLOCK.get()).build(null)
            );
}
