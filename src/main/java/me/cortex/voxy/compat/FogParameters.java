package me.cortex.voxy.compat;

import com.mojang.blaze3d.systems.RenderSystem;

//1.21.1 port: mirrors the parts of net.caffeinemc.mods.sodium.client.util.FogParameters used by voxy,
// 1.21.1 only has a single fog, so it is reported as the environmental fog (see MixinFogRenderer)
public record FogParameters(float red, float green, float blue, float alpha, float environmentalStart, float environmentalEnd) {
    public static FogParameters fromRenderSystem() {
        float[] colour = RenderSystem.getShaderFogColor();
        return new FogParameters(colour[0], colour[1], colour[2], colour[3], RenderSystem.getShaderFogStart(), RenderSystem.getShaderFogEnd());
    }
}
