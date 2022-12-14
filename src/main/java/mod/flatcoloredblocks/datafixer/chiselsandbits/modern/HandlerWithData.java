package mod.flatcoloredblocks.datafixer.chiselsandbits.modern;

import mod.chiselsandbits.storage.IStorageHandler;
import net.minecraft.nbt.CompoundTag;

public record HandlerWithData(IStorageHandler<?> handler, CompoundTag data) {}
