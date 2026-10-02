package me.cortex.voxy.client.mixin.iris;

import com.google.common.collect.ImmutableSet;
import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

//1.21.1 port: iris 1.8 (the last iris for 1.21.1) only supports colortex0-15, newer iris supports 32.
// Shaderpacks with voxy support (e.g. complementary, photon) use colortex16+ for their voxy buffers.
// Every other render target array in iris is sized from this set, so raising it is enough (same as iris 1.10)
@Mixin(value = PackRenderTargetDirectives.class, remap = false)
public class MixinPackRenderTargetDirectives {
    @Shadow @Final @Mutable public static Set<Integer> BASELINE_SUPPORTED_RENDER_TARGETS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void voxy$raiseRenderTargetLimit(CallbackInfo ci) {
        if (BASELINE_SUPPORTED_RENDER_TARGETS.size() >= 32) return;
        var builder = ImmutableSet.<Integer>builder();
        for (int i = 0; i < 32; i++) {
            builder.add(i);
        }
        BASELINE_SUPPORTED_RENDER_TARGETS = builder.build();
    }
}
