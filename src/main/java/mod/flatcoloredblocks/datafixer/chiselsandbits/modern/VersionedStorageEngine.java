package mod.flatcoloredblocks.datafixer.chiselsandbits.modern;

import com.mojang.logging.LogUtils;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.storage.IStorageHandler;
import mod.chiselsandbits.storage.IThreadAwareStorageEngine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class VersionedStorageEngine implements IThreadAwareStorageEngine
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private final int minimalVersion;
    private final List<IStorageHandler<?>> handlers;

    private final int currentVersion;
    private final IStorageHandler<?> saveHandler;

    VersionedStorageEngine(final LinkedList<IStorageHandler<?>> handlers) {
        this(0, handlers);
    }

    VersionedStorageEngine(final int minimalVersion, final LinkedList<IStorageHandler<?>> handlers) {
        Validate.notEmpty(handlers);

        this.minimalVersion = minimalVersion;
        this.handlers = Collections.unmodifiableList(handlers);

        this.saveHandler = handlers.getLast();
        this.currentVersion = minimalVersion + handlers.size() - 1;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compoundTag = new CompoundTag();

        serializeNBTInto(compoundTag);

        return compoundTag;
    }

    @Override
    public void serializeNBTInto(final CompoundTag tag)
    {
        tag.putInt(NbtConstants.VERSION, currentVersion);
        tag.put(NbtConstants.DATA, this.saveHandler.serializeNBT());
    }

    @Override
    public void deserializeNBT(final @NotNull CompoundTag nbt)
    {
        final IStorageHandler<?> storageHandler = readStorageHandler(nbt);
        if (storageHandler == null) return;
        storageHandler.deserializeNBT(nbt.getCompound(NbtConstants.DATA));
    }

    @Nullable
    private IStorageHandler<?> readStorageHandler(@NotNull CompoundTag nbt) {
        if (nbt.isEmpty())
        {
            LOGGER.warn("Empty NBT tag received, ignoring.");
            return null;
        }

        if (!nbt.contains(NbtConstants.VERSION))
            LOGGER.warn("The given NBT did not contain a versioned storage data entry. Missing the version!");

        final int version = nbt.contains(NbtConstants.VERSION) ? nbt.getInt(NbtConstants.VERSION) : minimalVersion;
        if (version < minimalVersion)
            throw new IllegalArgumentException("The given NBT did contained a version storage data entry, which is of an unsupported version. The version is " + version + ", but the minimal version is " + minimalVersion);

        if (version > currentVersion)
            throw new IllegalArgumentException("The given NBT did contained a version storage data entry, which is of an unsupported version. The version is " + version + ", but the current version is " + currentVersion);

        final int index = version - minimalVersion;
        return handlers.get(index);
    }

    @Override
    public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeNbt(serializeNBT());
    }

    @Override
    public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
    {
        deserializeNBT(Objects.requireNonNull(packetBuffer.readAnySizeNbt()));
    }

    @Override
    public Collection<? extends IStorageHandler<?>> getHandlers()
    {
        return handlers;
    }

    @Override
    public CompletableFuture<Void> deserializeOffThread(CompoundTag tag, Executor ioExecutor, Executor gameExecutor) {
        final IStorageHandler<?> storageHandler = readStorageHandler(tag);
        if (storageHandler == null) return CompletableFuture.completedFuture(null);
        return doDeserializeOffThread(storageHandler, tag, ioExecutor, gameExecutor);
    }

    private <P> CompletableFuture<Void> doDeserializeOffThread(IStorageHandler<P> handler, CompoundTag tag, Executor ioExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(
                () -> handler.readPayloadOffThread(tag.getCompound(NbtConstants.DATA)),
                ioExecutor
        ).thenAcceptAsync(handler::syncPayloadOnGameThread, gameExecutor);
    }

    public HandlerWithData getThreadAwareStorageHandler(CompoundTag tag) {
        var handler = readStorageHandler(tag);

        if (handler == null)
            throw new IllegalStateException("Tried to asynchronously deserialize data from a storage handler which is not thread aware.");

        return new HandlerWithData(handler, tag.getCompound(NbtConstants.DATA));
    }
}
