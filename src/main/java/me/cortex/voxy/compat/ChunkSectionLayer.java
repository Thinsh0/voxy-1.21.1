package me.cortex.voxy.compat;

import net.minecraft.client.renderer.RenderType;

//1.21.1 port: mirrors net.minecraft.client.renderer.chunk.ChunkSectionLayer (1.21.6+), 1.21.1 uses RenderType instead.
// cutout mipped is folded into CUTOUT as newer versions no longer have a separate layer for it
public enum ChunkSectionLayer {
    SOLID,
    CUTOUT,
    TRANSLUCENT,
    TRIPWIRE;

    public static ChunkSectionLayer of(RenderType type) {
        if (type == RenderType.solid()) return SOLID;
        if (type == RenderType.cutout() || type == RenderType.cutoutMipped()) return CUTOUT;
        if (type == RenderType.translucent()) return TRANSLUCENT;
        if (type == RenderType.tripwire()) return TRIPWIRE;
        throw new IllegalArgumentException("Not a chunk render type: " + type);
    }
}
