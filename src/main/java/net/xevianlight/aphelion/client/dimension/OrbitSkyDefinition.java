package net.xevianlight.aphelion.client.dimension;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record OrbitSkyDefinition(
        ResourceLocation id,
        Vec3 skyColor

) {

}
