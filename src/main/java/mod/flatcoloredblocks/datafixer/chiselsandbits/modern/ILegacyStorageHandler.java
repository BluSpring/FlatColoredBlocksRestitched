package mod.flatcoloredblocks.datafixer.chiselsandbits.modern;

import mod.chiselsandbits.storage.IStorageHandler;
import mod.flatcoloredblocks.datafixer.chiselsandbits.modern.handlers.StoragePayload;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public interface ILegacyStorageHandler extends IStorageHandler<StoragePayload> {
    /**
     * Checks if the given tag is a legacy tag that this handler can handle.
     * The tag is not guaranteed to be valid for this handler, but that is what this method needs to determine,
     * as such it is not allowed to read or write to or from the given tag.
     *
     * @param compoundTag The compound tag in question.
     * @return {@code true} if the tag is a legacy tag that this handler can handle.
     */
    boolean matches(@NotNull final CompoundTag compoundTag);

    @Override
    default CompoundTag serializeNBT() {
        throw new UnsupportedOperationException("Legacy storage does not support serialization.");
    }
}
