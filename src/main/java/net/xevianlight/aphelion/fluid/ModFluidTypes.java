package net.xevianlight.aphelion.fluid;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.xevianlight.aphelion.Aphelion;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ModFluidTypes {
    public static final ResourceLocation WATER_STILL_RL = ResourceLocation.parse("block/water_still");
    public static final ResourceLocation WATER_FLOWING_RL = ResourceLocation.parse("block/water_flow");
    public static final ResourceLocation WATER_OVERLAY_RL = ResourceLocation.parse("block/water_overlay");

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Aphelion.MOD_ID);

    public static final Supplier<FluidType> OIL_FLUID_TYPE = registerFluidType("oil",
            new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, WATER_OVERLAY_RL, 0xFF101010,
                    new Vector3f(10f / 255f, 10f / 255f, 10f / 255f),
                    FluidType.Properties.create().canDrown(true), 0f, 2f
            )
    );

    private static Supplier<FluidType> registerFluidType(String name, FluidType fluidType) {
        return FLUID_TYPES.register(name, () -> fluidType);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}
