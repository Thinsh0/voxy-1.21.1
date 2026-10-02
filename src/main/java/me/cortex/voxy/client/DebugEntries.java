package me.cortex.voxy.client;

import me.cortex.voxy.client.core.IGetVoxyRenderSystem;
import me.cortex.voxy.client.core.VoxyRenderSystem;
import me.cortex.voxy.client.core.util.GPUTiming;
import me.cortex.voxy.commonImpl.VoxyCommon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

import java.util.List;

//1.21.1 port: there are no debug screen entries before 1.21.9, the lines are appended to the F3 overlay
// by MixinDebugScreenOverlay, gpu debug follows the F3 profiler chart (shift+F3)
public class DebugEntries {
    public static void init() {
    }

    public static void addLines(List<String> lines) {
        if (!VoxyCommon.isAvailable()) {
            lines.add(ChatFormatting.RED + "voxy-"+VoxyCommon.MOD_VERSION);//Voxy installed, not avalible
            return;
        }
        var instance = VoxyCommon.getInstance();
        if (instance == null) {
            lines.add(ChatFormatting.YELLOW + "voxy-" + VoxyCommon.MOD_VERSION);//Voxy avalible, no instance active
            return;
        }
        VoxyRenderSystem vrs = null;
        var wr = Minecraft.getInstance().levelRenderer;
        if (wr != null) vrs = ((IGetVoxyRenderSystem) wr).voxy$getRenderSystem();

        //Voxy instance active
        lines.add((vrs==null?ChatFormatting.DARK_GREEN:ChatFormatting.GREEN)+"voxy-"+VoxyCommon.MOD_VERSION);

        lines.add("");
        instance.addDebug(lines);
        if (vrs != null) {
            lines.add("");
            vrs.addDebugInfo(lines);
        }
    }

    private static boolean previousGpuDebugEnabled = false;
    public static void onRebuild(boolean gpuDebugEnabled) {
        if (gpuDebugEnabled!=previousGpuDebugEnabled) {
            previousGpuDebugEnabled ^= true;

            GPUTiming.INSTANCE.setEnabled(previousGpuDebugEnabled);
            RenderStatistics.enabled = previousGpuDebugEnabled;
            var renderer = Minecraft.getInstance().levelRenderer;
            if (renderer!=null)renderer.allChanged();
        }
    }
}
