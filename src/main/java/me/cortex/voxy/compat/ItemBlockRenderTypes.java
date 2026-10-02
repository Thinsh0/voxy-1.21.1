package me.cortex.voxy.compat;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

//1.21.1 port: mirrors the ChunkSectionLayer returning methods of net.minecraft.client.renderer.ItemBlockRenderTypes (1.21.6+)
public final class ItemBlockRenderTypes {
    private ItemBlockRenderTypes() {}

    public static ChunkSectionLayer getChunkRenderType(BlockState state) {
        return ChunkSectionLayer.of(net.minecraft.client.renderer.ItemBlockRenderTypes.getChunkRenderType(state));
    }

    public static ChunkSectionLayer getRenderLayer(FluidState state) {
        return ChunkSectionLayer.of(net.minecraft.client.renderer.ItemBlockRenderTypes.getRenderLayer(state));
    }
}
