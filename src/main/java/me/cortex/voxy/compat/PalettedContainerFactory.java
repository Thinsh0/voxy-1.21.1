package me.cortex.voxy.compat;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;

//1.21.1 port: mirrors net.minecraft.world.level.chunk.PalettedContainerFactory (1.21.9+)
public record PalettedContainerFactory(Codec<PalettedContainerRO<Holder<Biome>>> biomeContainerCodec,
                                       Codec<PalettedContainer<BlockState>> blockStatesContainerCodec) {
    public static PalettedContainerFactory create(RegistryAccess access) {
        var biomes = access.registryOrThrow(Registries.BIOME);
        return new PalettedContainerFactory(
                PalettedContainer.codecRO(biomes.asHolderIdMap(), biomes.holderByNameCodec(), PalettedContainer.Strategy.SECTION_BIOMES, biomes.getHolderOrThrow(Biomes.PLAINS)),
                PalettedContainer.codecRW(Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState()));
    }
}
