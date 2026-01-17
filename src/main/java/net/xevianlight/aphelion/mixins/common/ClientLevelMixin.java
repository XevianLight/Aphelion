package net.xevianlight.aphelion.mixins.common;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.xevianlight.aphelion.client.dimension.DimensionRendererCache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.ClientLevelData.class)
public abstract class ClientLevelMixin {

    @Inject(method = "getHorizonHeight", at = @At("HEAD"), cancellable = true)
    private void aphelion$horizonHeight(LevelHeightAccessor level, CallbackInfoReturnable<Double> cir) {
        var mc = Minecraft.getInstance();
        var clientLevel = mc.level;
        if (clientLevel == null) return;

        // effectsLocation is what your dimension JSON sets in "effects"
        ResourceLocation effectsId = clientLevel.dimensionType().effectsLocation();

        var i = DimensionRendererCache.getOrDefault(effectsId);

        cir.setReturnValue((i == null) ? 1.0F : i.horizonHeight());
    }

    @Inject(method = "getClearColorScale", at = @At("HEAD"), cancellable = true)
    private void aphelion$clearColorScale(CallbackInfoReturnable<Float> cir) {
        var mc = Minecraft.getInstance();
        var clientLevel = mc.level;
        if (clientLevel == null) return;

        ResourceLocation effectsId = clientLevel.dimensionType().effectsLocation();

        var i = DimensionRendererCache.getOrDefault(effectsId);

        cir.setReturnValue((i == null) ? 1.0F : i.clearColorScale());
    }
}