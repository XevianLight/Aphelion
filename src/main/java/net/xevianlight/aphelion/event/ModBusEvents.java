package net.xevianlight.aphelion.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.dummy.entity.BaseMultiblockDummyBlockEntity;
import net.xevianlight.aphelion.block.entity.custom.ElectricArcFurnaceEntity;
import net.xevianlight.aphelion.block.entity.custom.TestBlockEntity;
import net.xevianlight.aphelion.block.entity.custom.VacuumArcFurnaceControllerEntity;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import net.xevianlight.aphelion.core.init.ModEntities;
import net.xevianlight.aphelion.entites.vehicles.RocketEntity;
import net.xevianlight.aphelion.network.RocketPayloadHandlers;
import net.xevianlight.aphelion.network.PartitionPayloadHandler;
import net.xevianlight.aphelion.network.packet.PartitionPayload;
import net.xevianlight.aphelion.network.packet.RocketLaunchPayload;

@EventBusSubscriber(modid = Aphelion.MOD_ID)
public class ModBusEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.TEST_BLOCK_ENTITY.get(), TestBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ELECTRIC_ARC_FURNACE_ENTITY.get(), ElectricArcFurnaceEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ELECTRIC_ARC_FURNACE_ENTITY.get(), ElectricArcFurnaceEntity::getEnergyStorage);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.VACUUM_ARC_FURNACE_ENTITY.get(), VacuumArcFurnaceControllerEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.VACUUM_ARC_FURNACE_ENTITY.get(), VacuumArcFurnaceControllerEntity::getEnergyStorage);
//        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.VAF_MULTIBLOCK_DUMMY_ENTITY.get(), VAFMultiblockDummyBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.VAF_MULTIBLOCK_DUMMY_ENTITY.get(), BaseMultiblockDummyBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.VAF_MULTIBLOCK_DUMMY_ENTITY.get(), BaseMultiblockDummyBlockEntity::getEnergyStorage);

        event.registerEntity(Capabilities.ItemHandler.ENTITY, ModEntities.ROCKET.get(), (rocket, ctx) -> rocket.getInventory());
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1")
                .executesOn(HandlerThread.MAIN);

        registrar.playToClient(
                PartitionPayload.TYPE,
                PartitionPayload.STREAM_CODEC,
                PartitionPayloadHandler::handleDataOnMain
        );

        registrar.playToServer(
                RocketLaunchPayload.TYPE,
                RocketLaunchPayload.STREAM_CODEC,
                RocketPayloadHandlers::handleRocketLaunch
        );

    }
}
