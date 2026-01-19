package net.xevianlight.aphelion.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.entity.custom.ElectricArcFurnaceEntity;
import net.xevianlight.aphelion.block.entity.custom.TestBlockEntity;
import net.xevianlight.aphelion.block.entity.custom.VacuumArcFurnaceControllerEntity;
import net.xevianlight.aphelion.core.init.ModBlockEntities;
import net.xevianlight.aphelion.network.ServerPayloadHandler;
import net.xevianlight.aphelion.network.packet.PartitionData;

@EventBusSubscriber(modid = Aphelion.MOD_ID)
public class ModBusEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.TEST_BLOCK_ENTITY.get(), TestBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ELECTRIC_ARC_FURNACE_ENTITY.get(), ElectricArcFurnaceEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ELECTRIC_ARC_FURNACE_ENTITY.get(), ElectricArcFurnaceEntity::getEnergyStorage);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.VACUUM_ARC_FURNACE_ENTITY.get(), VacuumArcFurnaceControllerEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.VACUUM_ARC_FURNACE_ENTITY.get(), VacuumArcFurnaceControllerEntity::getEnergyStorage);
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1")
                .executesOn(HandlerThread.MAIN);

        registrar.playToClient(
                PartitionData.TYPE,
                PartitionData.STREAM_CODEC,
                ServerPayloadHandler::handleDataOnMain);

    }
}
