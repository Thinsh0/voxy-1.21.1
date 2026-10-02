package me.cortex.voxy.client.mixin.minecraft;

import me.cortex.voxy.client.DebugEntries;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

//1.21.1 port: replaces MixinDebugScreenEntryList
@Mixin(DebugScreenOverlay.class)
public abstract class MixinDebugScreenOverlay {
    @Shadow public abstract boolean showDebugScreen();
    @Shadow public abstract boolean showProfilerChart();

    @Inject(method = "getSystemInformation", at = @At("RETURN"))
    private void voxy$injectDebug(CallbackInfoReturnable<List<String>> cir) {
        var lines = cir.getReturnValue();
        lines.add("");
        DebugEntries.addLines(lines);
    }

    @Inject(method = {"toggleOverlay", "toggleProfilerChart", "reset"}, at = @At("TAIL"))
    private void voxy$updateGpuDebug(CallbackInfo ci) {
        DebugEntries.onRebuild(this.showDebugScreen() && this.showProfilerChart());
    }
}
