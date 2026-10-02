package me.cortex.voxy.client.mixin.minecraft;

import com.mojang.blaze3d.systems.RenderSystem;
import me.cortex.voxy.client.config.VoxyConfig;
import me.cortex.voxy.client.core.IGetVoxyRenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//1.21.1 port: there is a single fog instead of separate environmental and render distance fogs,
// so work out which one vanilla produced and disable it like upstream does
@Mixin(value = FogRenderer.class,remap = true)
public class MixinFogRenderer {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void voxy$modifyFog(Camera camera, FogRenderer.FogMode mode, float renderDistance, boolean thickFog, float pTick, CallbackInfo ci) {
        if (mode != FogRenderer.FogMode.FOG_TERRAIN) return;
        if (!VoxyConfig.CONFIG.isRenderingEnabled()) return;

        var vrs = IGetVoxyRenderSystem.getNullable();
        if (vrs == null) return;

        boolean environmental = thickFog || camera.getFluidInCamera() != FogType.NONE ||
                (camera.getEntity() instanceof LivingEntity entity && (entity.hasEffect(MobEffects.BLINDNESS) || entity.hasEffect(MobEffects.DARKNESS)));
        boolean fogIsDamnClose = RenderSystem.getShaderFogEnd()<10;
        if (environmental && (VoxyConfig.CONFIG.useEnvironmentalFog || fogIsDamnClose)) {
            return;
        }

        RenderSystem.setShaderFogStart(99999999);
        RenderSystem.setShaderFogEnd(99999999);
    }
}
