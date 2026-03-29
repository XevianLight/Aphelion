package net.xevianlight.aphelion.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class ModFluidTags {
    public static final TagKey<Fluid> ROCKET_FUEL = create("rocket_fuel");

    private static TagKey<Fluid> create(String name) {
        return TagKey.create(Registries.FLUID, ResourceLocation.withDefaultNamespace(name));
    }

    public static TagKey<Fluid> create(ResourceLocation name) {
        return TagKey.create(Registries.FLUID, name);
    }
}
