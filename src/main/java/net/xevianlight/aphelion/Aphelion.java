package net.xevianlight.aphelion;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.xevianlight.aphelion.block.dummy.renderer.MultiblockDummyRenderer;
import net.xevianlight.aphelion.block.entity.custom.renderer.OxygenTestRenderer;
import net.xevianlight.aphelion.client.AphelionConfig;
import net.xevianlight.aphelion.planet.AphelionPlanetJSONLoader;
import net.xevianlight.aphelion.core.init.*;
import net.xevianlight.aphelion.fluid.BaseFluidType;
import net.xevianlight.aphelion.fluid.ModFluidTypes;
import net.xevianlight.aphelion.fluid.ModFluids;
import net.xevianlight.aphelion.recipe.ModRecipes;
import net.xevianlight.aphelion.screen.ElectricArcFurnaceScreen;
import net.xevianlight.aphelion.screen.ModMenuTypes;
import net.xevianlight.aphelion.screen.TestBlockScreen;
import net.xevianlight.aphelion.screen.VacuumArcFurnaceScreen;
import org.slf4j.Logger;
import net.xevianlight.aphelion.entites.vehicles.RocketRenderer;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Aphelion.MOD_ID)
public class Aphelion {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "aphelion";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static IEventBus MOD_BUS = null;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Aphelion(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        MOD_BUS = modEventBus;

        ModItems.ITEMS.register(MOD_BUS);
        ModBlocks.BLOCKS.register(MOD_BUS);
        ModCreativeTabs.CREATIVE_MODE_TAB.register(MOD_BUS);
        ModBlockEntities.BLOCK_ENTITIES.register(MOD_BUS);
        ModMenuTypes.MENUS.register(MOD_BUS);
        ModFluidTypes.FLUID_TYPES.register(MOD_BUS);
        ModFluids.register(MOD_BUS);
        ModSounds.register(MOD_BUS);
        ModRecipes.register(MOD_BUS);
        ModEntities.register(MOD_BUS);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExtremeRocketry) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        MOD_BUS.addListener(this::addCreative);

//        MOD_BUS.addListener(this::registerCommands);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, AphelionConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, path);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.TEST_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.TEST_BLOCK);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");


    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        // Set up the planet json listener. This reloads on /reload
        event.addListener(new AphelionPlanetJSONLoader());
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
//                ItemBlockRenderTypes.setRenderLayer(ModFluids.SOURCE_OIL_FLUID.get(), RenderType.translucent());
//                ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_OIL_FLUID.get(), RenderType.translucent());
            });
        }

        @SubscribeEvent
        public static void onClientExtensions(RegisterClientExtensionsEvent event) {
            event.registerFluidType(((BaseFluidType) ModFluidTypes.OIL_FLUID_TYPE.get()).getClientFluidTypeExtensions(), ModFluidTypes.OIL_FLUID_TYPE.get());
        }

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.VAF_MULTIBLOCK_DUMMY_ENTITY.get(), MultiblockDummyRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.OXYGEN_TEST_BLOCK_ENTITY.get(), OxygenTestRenderer::new);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.TEST_BLOCK_MENU.get(), TestBlockScreen::new);
            event.register(ModMenuTypes.ELECTRIC_ARC_FURNACE_MENU.get(), ElectricArcFurnaceScreen::new);
            event.register(ModMenuTypes.VACUUM_ARC_FURNACE_MENU.get(), VacuumArcFurnaceScreen::new);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.ROCKET.get(), RocketRenderer::new);
        }
    }
}
