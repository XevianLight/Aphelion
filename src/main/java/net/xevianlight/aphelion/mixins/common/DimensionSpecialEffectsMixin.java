package net.xevianlight.aphelion.mixins.common;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.xevianlight.aphelion.client.dimension.DimensionRendererCache;
import net.xevianlight.aphelion.client.dimension.DimensionSkyEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionSpecialEffects.class)
public abstract class DimensionSpecialEffectsMixin {

    @Inject(method = "forType", at = @At("HEAD"), cancellable = true)
    private static void aphelion$forType(DimensionType type, CallbackInfoReturnable<DimensionSpecialEffects> cir) {
        ResourceLocation effectsId = type.effectsLocation();

        if (DimensionRendererCache.RENDERERS.containsKey(effectsId)) {
            cir.setReturnValue(new DimensionSkyEffects(effectsId));
        }
    }
}
