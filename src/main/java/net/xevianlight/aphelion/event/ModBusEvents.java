package net.xevianlight.aphelion.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.block.entity.custom.ElectricArcFurnaceEntity;
import net.xevianlight.aphelion.block.entity.custom.TestBlockEntity;
import net.xevianlight.aphelion.core.init.ModBlockEntities;

@EventBusSubscriber(modid = Aphelion.MOD_ID)
public class ModBusEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.TEST_BLOCK_ENTITY.get(), TestBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ELECTRIC_ARC_FURNACE_ENTITY.get(), ElectricArcFurnaceEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ELECTRIC_ARC_FURNACE_ENTITY.get(), ElectricArcFurnaceEntity::getEnergyStorage);
    }
}
