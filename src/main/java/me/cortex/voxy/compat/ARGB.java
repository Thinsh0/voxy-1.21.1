package me.cortex.voxy.compat;

//1.21.1 port: mirrors the parts of net.minecraft.util.ARGB (1.21.2+) used by voxy
public final class ARGB {
    private ARGB() {}

    //Linear [0,1] to an 8 bit sRGB channel
    public static int linearToSrgbChannel(float linear) {
        float c = Math.clamp(linear, 0f, 1f);
        float srgb = c <= 0.0031308f ? c * 12.92f : 1.055f * (float) Math.pow(c, 1 / 2.4) - 0.055f;
        return Math.round(srgb * 255f);
    }
}
