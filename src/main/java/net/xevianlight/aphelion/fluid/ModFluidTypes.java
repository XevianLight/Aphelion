package net.xevianlight.aphelion.fluid;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.xevianlight.aphelion.Aphelion;
import org.joml.Vector3f;

import java.awt.*;
import java.util.function.Supplier;

public class ModFluidTypes {
    public static final ResourceLocation WATER_STILL_RL =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still");

    public static final ResourceLocation WATER_FLOWING_RL =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow");

    public static final ResourceLocation WATER_OVERLAY_RL =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_overlay");

    static final Color oilColor = new Color(10, 10, 10, 255);
    static final Color rocketFuelColor = new Color(73, 59, 28, 255);

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Aphelion.MOD_ID);

    public static final Supplier<FluidType> OIL_FLUID_TYPE = registerFluidType("oil",
            new BaseFluidType(
                    WATER_STILL_RL,
                    WATER_FLOWING_RL,
                    WATER_OVERLAY_RL,
                    oilColor.getRGB(),
                    colToVec(oilColor),
                    FluidType.Properties.create().canDrown(true),
                    0f,
                    0.5f
            )
    );


    public static final Supplier<FluidType> ROCKET_FUEL_FLUID_TYPE = registerFluidType("rocket_fuel",
            new BaseFluidType(
                    WATER_STILL_RL,
                    WATER_FLOWING_RL,
                    WATER_OVERLAY_RL,
                    rocketFuelColor.getRGB(),
                    colToVec(rocketFuelColor),
                    FluidType.Properties.create().canDrown(true),
                    0f,
                    2f)
    );

    private static Supplier<FluidType> registerFluidType(String name, FluidType fluidType) {
        return FLUID_TYPES.register(name, () -> fluidType);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }

    public static Vector3f colToVec (Color color) {
        return new Vector3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
    }
}
