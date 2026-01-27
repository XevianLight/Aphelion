package net.xevianlight.aphelion.core.init;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.bus.api.IEventBus;
import net.xevianlight.aphelion.Aphelion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Aphelion.MOD_ID);

    private static ResourceKey<JukeboxSong> createSong(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, name));
    }

    public static final Supplier<SoundEvent> BIT_SHIFT = registerSoundEvent("bit_shift");
    public static final  ResourceKey<JukeboxSong> BIT_SHIFT_KEY = createSong("bit_shift");

    public static final Supplier<SoundEvent> ROCKET_ENGINE = registerSoundEvent("rocket_engine");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Aphelion.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
