package me.cortex.voxy.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;

//1.21.1 port: the Optional/default based CompoundTag getters only exist from 1.21.5
public final class NbtCompat {
    private NbtCompat() {}

    public static int getIntOr(CompoundTag tag, String key, int def) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getInt(key) : def;
    }

    public static String getStringOr(CompoundTag tag, String key, String def) {
        return tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : def;
    }

    public static Optional<CompoundTag> getCompound(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_COMPOUND) ? Optional.of(tag.getCompound(key)) : Optional.empty();
    }

    public static Optional<ListTag> getList(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_LIST) ? Optional.of((ListTag) tag.get(key)) : Optional.empty();
    }

    public static Optional<byte[]> getByteArray(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_BYTE_ARRAY) ? Optional.of(tag.getByteArray(key)) : Optional.empty();
    }
}
