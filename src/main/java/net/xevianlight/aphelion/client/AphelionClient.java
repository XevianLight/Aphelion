package net.xevianlight.aphelion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.xevianlight.aphelion.Aphelion;
import net.xevianlight.aphelion.client.dimension.AphelionDimensionRenderers;

import java.util.function.BiConsumer;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Aphelion.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Aphelion.MOD_ID, value = Dist.CLIENT)
public class AphelionClient {
    public AphelionClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        Aphelion.LOGGER.info("HELLO FROM CLIENT SETUP");
        Aphelion.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void onClientReloadListeners(RegisterClientReloadListenersEvent event) {
        onAddReloadListener((id, listener) -> event.registerReloadListener(listener));
    }

    public static void onAddReloadListener(BiConsumer<ResourceLocation, PreparableReloadListener> consumer) {
        consumer.accept(ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "planet_renderers"), new AphelionDimensionRenderers());
    }

    @SubscribeEvent
    public static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, "space"),
                new net.xevianlight.aphelion.client.dimension.SpaceSkyEffects()
        );
    }
}
